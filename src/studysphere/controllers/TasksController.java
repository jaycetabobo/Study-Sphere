package studysphere.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import studysphere.models.TaskItem;
import studysphere.services.MockDataService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class TasksController {
    @FXML
    private ImageView bgTasks;
    @FXML
    private StackPane tasksStack;

    @FXML
    private ListView<TaskItem> taskList;
    @FXML
    private Label taskTitle, taskDue, taskPriority;
    @FXML
    private Label taskSubjectLabel;
    @FXML
    private TextArea taskDescriptionArea;
    @FXML
    private CheckBox taskCompleted;
    @FXML
    private VBox emptyPlaceholder;

    // new task fields (may not exist in tasks.fxml)
    @FXML
    private TextField newTaskTitle;
    @FXML
    private DatePicker newTaskDate;
    @FXML
    private ChoiceBox<String> newTaskPriority;
    @FXML
    private ComboBox<String> newTaskStatus;
    @FXML
    private TextField newTaskSubject;
    @FXML
    private TextArea newTaskDescription;

    // filter fields
    @FXML private ComboBox<String> filterStatus, filterPriority, filterSubject;

    private ObservableList<TaskItem> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // bind bg to stack
        if (bgTasks != null && tasksStack != null){
            bgTasks.fitWidthProperty().bind(tasksStack.widthProperty());
            bgTasks.fitHeightProperty().bind(tasksStack.heightProperty());
        }

        refreshList();

        if (taskList != null) {
            // make sure ListView can expand to fill available vertical space
            taskList.setPrefHeight(0);
            taskList.setMaxHeight(Double.MAX_VALUE);
            javafx.scene.layout.VBox.setVgrow(taskList, javafx.scene.layout.Priority.ALWAYS);

            taskList.setCellFactory(lv -> new ListCell<TaskItem>(){
                private VBox vbox = new VBox();
                private Label titleLbl = new Label();
                private Label previewLbl = new Label();
                {
                    titleLbl.getStyleClass().add("note-title");
                    previewLbl.getStyleClass().add("note-preview");
                    previewLbl.setWrapText(true);
                    vbox.getChildren().addAll(titleLbl, previewLbl);
                    vbox.setSpacing(6);
                    vbox.setPadding(new javafx.geometry.Insets(12,0,12,12));
                }
                @Override
                protected void updateItem(TaskItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null); setGraphic(null);
                    } else {
                        titleLbl.setText(item.getTitle());
                        String preview = (item.getDescription() == null) ? "" : item.getDescription();
                        if (preview.length() > 160) preview = preview.substring(0,157) + "...";
                        previewLbl.setText(preview);
                        // bind preview label max width to the list view width so text wraps and no horizontal scrollbar appears
                        try {
                            if (getListView() != null && !previewLbl.maxWidthProperty().isBound()) {
                                previewLbl.maxWidthProperty().bind(getListView().widthProperty().subtract(48));
                            }
                        } catch (Exception ignore) {}
                        setGraphic(vbox);
                    }
                }
            });

            taskList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    taskTitle.setText(newV.getTitle());
                    taskDue.setText(newV.getDueDate() == null ? "" : newV.getDueDate().toLocalDate().toString());
                    taskPriority.setText(newV.getPriority());
                    taskSubjectLabel.setText(newV.getSubject() == null ? "" : newV.getSubject());
                    taskDescriptionArea.setText(newV.getDescription() == null ? "" : newV.getDescription());
                    taskCompleted.setSelected(newV.isCompleted());
                } else {
                    taskTitle.setText("");
                    taskDue.setText("");
                    taskPriority.setText("");
                    taskSubjectLabel.setText("");
                    taskDescriptionArea.setText("");
                    taskCompleted.setSelected(false);
                }
            });
        }

        if (newTaskPriority != null) {
            newTaskPriority.setItems(FXCollections.observableArrayList("High","Medium","Low"));
        }
        // populate status ComboBox so it has dropdown items
        if (newTaskStatus != null) {
            newTaskStatus.setItems(FXCollections.observableArrayList("To-do", "In Progress", "Done", "Archive"));
            newTaskStatus.getSelectionModel().selectFirst();
            newTaskStatus.setDisable(false);
            newTaskStatus.setMouseTransparent(false);
        }
        // set prompt for DatePicker editor so it shows dd/mm/yyyy placeholder
        if (newTaskDate != null && newTaskDate.getEditor() != null) {
            newTaskDate.getEditor().setPromptText("dd/mm/yyyy");
        }
        // wire filters (chips) so the top UI matches the desired screenshot
        if (filterSubject != null) populateFilterSubjects();
        if (filterStatus != null) { filterStatus.setItems(FXCollections.observableArrayList("All Task","To-do","In Progress","Done")); filterStatus.getSelectionModel().selectFirst(); setupChipChoiceBox(filterStatus, ""); }
        if (filterPriority != null) { filterPriority.setItems(FXCollections.observableArrayList("All Priority","High","Medium","Low")); filterPriority.getSelectionModel().selectFirst(); setupChipChoiceBox(filterPriority, ""); }
    }

    private void refreshList() {
        items.setAll(MockDataService.getInstance().getAllTasks());
        if (taskList != null) taskList.setItems(items);
        // show empty placeholder when no tasks
        if (emptyPlaceholder != null) emptyPlaceholder.setVisible(items.isEmpty());
    }

    @FXML
    void onNewTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tasks_new.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            // create transparent modal like HomeController so visuals match dashboard
            Stage dialog = new Stage();
            if (taskList != null && taskList.getScene() != null && taskList.getScene().getWindow() != null) {
                dialog.initOwner(taskList.getScene().getWindow());
                dialog.initModality(Modality.WINDOW_MODAL);
            } else {
                dialog.initModality(Modality.APPLICATION_MODAL);
            }
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            javafx.scene.Group wrapper = new javafx.scene.Group(root);
            Scene scene = new Scene(wrapper);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            java.net.URL css = getClass().getResource("/fxml/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
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
            refreshList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSaveTask() {
        if (newTaskTitle == null) return;
        String title = newTaskTitle.getText();
        if (title != null && !title.isBlank()) {
            LocalDateTime due = (newTaskDate!=null && newTaskDate.getValue() != null) ? newTaskDate.getValue().atStartOfDay() : LocalDateTime.now().plusDays(1);
            String pri = (newTaskPriority!=null && newTaskPriority.getValue() != null) ? newTaskPriority.getValue() : "Low";
            TaskItem t = new TaskItem(UUID.randomUUID().toString(), title, due, pri);
            // set additional fields
            if (newTaskSubject != null) t.setSubject(newTaskSubject.getText());
            if (newTaskDescription != null) t.setDescription(newTaskDescription.getText());
            if (newTaskStatus != null && newTaskStatus.getValue() != null) t.setStatus(newTaskStatus.getValue());
            MockDataService.getInstance().addTask(t);
            Stage s = (Stage) newTaskTitle.getScene().getWindow();
            s.close();
        }
    }

    // hook for modal to request focus on the title field
    public void requestInitialFocus() {
        if (newTaskTitle != null) newTaskTitle.requestFocus();
    }

    @FXML
    void onCancelNewTask() {
        if (newTaskTitle == null) return;
        Stage s = (Stage) newTaskTitle.getScene().getWindow();
        s.close();
    }

    @FXML
    void onDeleteTask() {
        if (taskList == null) return;
        TaskItem sel = taskList.getSelectionModel().getSelectedItem();
        if (sel != null) {
            MockDataService.getInstance().removeTask(sel.getId());
            refreshList();
        }
    }

    @FXML
    void onToggleComplete() {
        if (taskList == null || taskCompleted==null) return;
        TaskItem sel = taskList.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setCompleted(taskCompleted.isSelected());
            refreshList();
        }
    }

    private void populateFilterSubjects(){
        if (filterSubject == null) return;
        // derive subjects from tasks' subject field
        java.util.Set<String> subjects = new java.util.HashSet<>();
        for (TaskItem t : MockDataService.getInstance().getAllTasks()) {
            if (t.getSubject() != null && !t.getSubject().isBlank()) subjects.add(t.getSubject());
        }
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("All Subjects");
        list.addAll(subjects);
        filterSubject.setItems(FXCollections.observableArrayList(list));
        filterSubject.getSelectionModel().selectFirst();
        setupChipChoiceBox(filterSubject, "📚");
    }

    // helper to style a ComboBox like a chip
    private void setupChipChoiceBox(ComboBox<String> cb, String icon) {
        if (cb == null) return;
        cb.setCellFactory(list -> new ListCell<String>(){
            @Override protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setStyle("-fx-padding:8 12 8 12;"); }
            }
        });
        cb.setButtonCell(new ListCell<String>(){ @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); if (empty||item==null){ setText(null); setGraphic(null);} else { setText(item); } } });
    }
}
