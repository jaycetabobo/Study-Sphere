package studysphere.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import studysphere.models.Note;
import studysphere.services.MockDataService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

public class NotesController {
    @FXML
    private ImageView bgNotes;
    @FXML
    private StackPane notesStack;
    @FXML
    private ListView<Note> notesList;
    @FXML
    private TextField noteTitle;
    @FXML
    private TextArea noteContent;
    @FXML
    private TextField noteTags;
    @FXML
    private TextField searchField;
    @FXML
    private javafx.scene.control.ComboBox<String> filterTag;
    @FXML
    private javafx.scene.control.ComboBox<String> filterSubject;
    @FXML
    private ScrollPane notesScroll;
    @FXML
    private StackPane contentStack;
    @FXML
    private VBox emptyPlaceholder;
    @FXML
    private ImageView emptyIcon;
    @FXML
    private Button notesInfo;

    private ObservableList<Note> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // bind bg to stack size
        if (bgNotes != null && notesStack != null){
            bgNotes.fitWidthProperty().bind(notesStack.widthProperty());
            bgNotes.fitHeightProperty().bind(notesStack.heightProperty());
        }

        refreshList();
        notesList.setCellFactory(lv -> new ListCell<Note>(){
            private VBox vbox = new VBox();
            private Label titleLbl = new Label();
            private Label previewLbl = new Label();
            {
                // Use CSS classes so styles are managed in styles.css
                titleLbl.getStyleClass().add("note-title");
                previewLbl.getStyleClass().add("note-preview");
                previewLbl.setWrapText(true);
                vbox.getChildren().addAll(titleLbl, previewLbl);
                vbox.setSpacing(4);
                vbox.setPadding(new javafx.geometry.Insets(8, 0, 8, 0));
            }
            @Override
            protected void updateItem(Note item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    titleLbl.setText(item.getTitle());
                    String preview = item.getContent() == null ? "" : item.getContent();
                    preview = preview.replaceAll("\\n"," ");
                    // keep a reasonably short preview but avoid over-truncation here; allow wrapping
                    if (preview.length() > 200) preview = preview.substring(0, 197) + "...";
                    previewLbl.setText(preview);
                    setGraphic(vbox);
                    // open modal on click (single click to match user's request)
                    setOnMouseClicked(evt -> {
                        if (evt.getClickCount() >= 1) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
                                Parent root = loader.load();
                                // set controller note
                                studysphere.controllers.NoteViewController ctrl = loader.getController();
                                ctrl.setNote(item);
                                // create transparent modal like HomeController.openModal to match dashboard modal styling
                                Stage dialog = new Stage();
                                if (notesList.getScene() != null && notesList.getScene().getWindow() != null) {
                                    dialog.initOwner(notesList.getScene().getWindow());
                                    dialog.initModality(Modality.WINDOW_MODAL);
                                } else {
                                    dialog.initModality(Modality.APPLICATION_MODAL);
                                }
                                dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                                javafx.scene.Group wrapper = new javafx.scene.Group(root);
                                Scene viewScene = new Scene(wrapper);
                                viewScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                                java.net.URL styleUrl = getClass().getResource("/fxml/styles.css");
                                if (styleUrl != null) viewScene.getStylesheets().add(styleUrl.toExternalForm());
                                dialog.setScene(viewScene);
                                dialog.setResizable(false);
                                dialog.sizeToScene();
                                // center and request focus if controller exposes helper
                                Object noteCtrl = loader.getController();
                                dialog.setOnShown(shownEvt -> {
                                    if (dialog.getOwner() != null) {
                                        dialog.setX(dialog.getOwner().getX() + (dialog.getOwner().getWidth() - dialog.getWidth()) / 2);
                                        dialog.setY(dialog.getOwner().getY() + (dialog.getOwner().getHeight() - dialog.getHeight()) / 2);
                                    }
                                    try {
                                        if (noteCtrl != null) {
                                            java.lang.reflect.Method m = noteCtrl.getClass().getMethod("requestInitialFocus");
                                            if (m != null) m.invoke(noteCtrl);
                                        }
                                    } catch (Exception ignore) {}
                                });
                                dialog.showAndWait();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                noteTitle.setText(newV.getTitle());
                noteContent.setText(newV.getContent());
                noteTags.setText(String.join(",", newV.getTags()));
            }
        });

        // search/filter - only wire if controls exist in this FXML
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter());
        }
        if (filterTag != null) {
            filterTag.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> applyFilter());
            populateFilterTags();
        } else {
            // still populate list without tag filters
            applyFilter();
        }
        if (filterSubject != null) {
            populateFilterSubjects();
            filterSubject.getSelectionModel().selectedItemProperty().addListener((obs,oldV,newV)->applyFilter());
        }
        // update placeholder visibility
        updatePlaceholder();
        // load placeholder icon if available (try classpath then fallback to local file)
        try {
            javafx.scene.image.Image img = null;
            java.net.URL url = getClass().getResource("/assets/SS logo 1.png");
            if (url != null) {
                img = new javafx.scene.image.Image(url.toExternalForm());
            } else {
                java.io.File f = new java.io.File("assets/SS logo 1.png");
                if (f.exists()) img = new javafx.scene.image.Image(f.toURI().toString());
            }
            if (img != null && emptyIcon != null) emptyIcon.setImage(img);
        } catch (Exception ignored) {}
        // simple info action
        if (notesInfo != null) notesInfo.setOnAction(e -> {
            Tooltip t = new Tooltip("Create and organize your study notes here.");
            Tooltip.install(notesInfo, t);
        });
    }

    private void populateFilterTags(){
        if (filterTag == null) return;
        Set<String> tags = MockDataService.getInstance().getAllNotes().stream().flatMap(n->n.getTags().stream()).collect(Collectors.toSet());
        List<String> list = new ArrayList<>(); list.add("All"); list.addAll(tags);
        filterTag.setItems(FXCollections.observableArrayList(list));
        filterTag.getSelectionModel().selectFirst();
        // render as chip
        setupChipChoiceBox(filterTag, "🏷");
    }

    private void populateFilterSubjects(){
        if (filterSubject == null) return;
        // subjects derived from note tags or explicit subject field if present
        Set<String> subjects = MockDataService.getInstance().getAllNotes().stream().flatMap(n->n.getTags().stream()).collect(Collectors.toSet());
        List<String> list = new ArrayList<>(); list.add("All Subjects"); list.addAll(subjects);
        filterSubject.setItems(FXCollections.observableArrayList(list));
        filterSubject.getSelectionModel().selectFirst();
        setupChipChoiceBox(filterSubject, "📚");
    }

    // small helper to style a ChoiceBox to look like a chip with an icon
    private void setupChipChoiceBox(javafx.scene.control.ComboBox<String> cb, String icon) {
        if (cb == null) return;
        cb.setCellFactory(list -> new ListCell<String>(){
            @Override protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setStyle("-fx-padding:8 12 8 12;"); }
            }
        });
        // button cell that shows the selected value similarly
        cb.setButtonCell(new ListCell<String>(){ @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); if (empty||item==null){ setText(null); setGraphic(null);} else { setText(item); } } });
    }

    private void applyFilter(){
        String q = (searchField!=null && searchField.getText()!=null)?searchField.getText().toLowerCase():"";
        String tag = (filterTag!=null)?filterTag.getValue():null;
        String subj = (filterSubject!=null)?filterSubject.getValue():null;
        List<Note> filtered = MockDataService.getInstance().getAllNotes().stream().filter(n->
                (tag==null || tag.equals("All") || n.getTags().contains(tag)) &&
                (subj==null || subj.equals("All Subjects") || n.getTags().contains(subj)) &&
                (q.isBlank() || n.getTitle().toLowerCase().contains(q) || n.getContent().toLowerCase().contains(q))
        ).collect(Collectors.toList());
        items.setAll(filtered);
        notesList.setItems(items);
        updatePlaceholder();
    }

    private void refreshList() {
        items.setAll(MockDataService.getInstance().getAllNotes());
        notesList.setItems(items);
        populateFilterTags();
        populateFilterSubjects();
        updatePlaceholder();
    }

    private void updatePlaceholder(){
        boolean empty = items.isEmpty();
        if (emptyPlaceholder!=null) emptyPlaceholder.setVisible(empty);
        if (notesScroll!=null) notesScroll.setVisible(!empty);
    }

    @FXML
    void onNewNote(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/notes_new.fxml"));
            Parent root = loader.load();
            // keep controller reference and set up stage/scene
            Object ctrl = loader.getController();
            // use transparent wrapped modal (same approach as HomeController.openModal)
            Stage dialog = new Stage();
            if (notesList.getScene() != null && notesList.getScene().getWindow() != null) {
                dialog.initOwner(notesList.getScene().getWindow());
                dialog.initModality(Modality.WINDOW_MODAL);
            } else {
                dialog.initModality(Modality.APPLICATION_MODAL);
            }
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            javafx.scene.Group wrapper = new javafx.scene.Group(root);
            Scene scene = new Scene(wrapper);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            java.net.URL styleUrl = getClass().getResource("/fxml/styles.css");
            if (styleUrl != null) scene.getStylesheets().add(styleUrl.toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.setOnShown(evt -> {
                if (dialog.getOwner() != null) {
                    dialog.setX(dialog.getOwner().getX() + (dialog.getOwner().getWidth() - dialog.getWidth()) / 2);
                    dialog.setY(dialog.getOwner().getY() + (dialog.getOwner().getHeight() - dialog.getHeight()) / 2);
                }
                try {
                    if (ctrl != null) {
                        java.lang.reflect.Method m = ctrl.getClass().getMethod("requestInitialFocus");
                        if (m != null) m.invoke(ctrl);
                    }
                } catch (Exception ignore) {}
            });
            dialog.showAndWait();
             // refresh list after dialog closes
             refreshList();
             populateFilterTags();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

    @FXML
    void onSaveNote() {
        Note sel = notesList.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setTitle(noteTitle.getText());
            sel.setContent(noteContent.getText());
            sel.getTags().clear();
            if (noteTags.getText()!=null && !noteTags.getText().isBlank()){
                String[] parts = noteTags.getText().split(",");
                for(String p: parts) sel.getTags().add(p.trim());
            }
            MockDataService.getInstance().addNote(sel); // save
            refreshList();
            populateFilterTags();
        }
    }

    @FXML
    void onDeleteNote() {
        Note sel = notesList.getSelectionModel().getSelectedItem();
        if (sel != null) {
            MockDataService.getInstance().removeNote(sel.getId());
            refreshList();
            noteTitle.clear();
            noteContent.clear();
            noteTags.clear();
            populateFilterTags();
        }
    }

    // handling new note
    @FXML
    private TextField noteTitleNew;
    @FXML
    private TextArea noteContentNew;

    @FXML
    void onSaveNoteNew() {
        String t = noteTitleNew.getText();
        String c = noteContentNew.getText();
        if (t != null && !t.isBlank()) {
            Note n = new Note(UUID.randomUUID().toString(), t, c);
            MockDataService.getInstance().addNote(n);
            // close the dialog window
            Stage s = (Stage) noteTitleNew.getScene().getWindow();
            s.close();
        }
    }

    @FXML
    void onCancelNewNote() {
        Stage s = (Stage) noteTitleNew.getScene().getWindow();
        s.close();
    }
}
