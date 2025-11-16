package studysphere.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import studysphere.services.MockDataService;
import studysphere.models.TaskItem;
import studysphere.models.Note;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class HomeController {
    @SuppressWarnings("unused")
    @FXML
    private VBox centerContainer;
    @SuppressWarnings("unused")
    @FXML
    private ScrollPane mainScroll;

    @SuppressWarnings("unused")
    @FXML
    private Label totalNotesLabel;
    @SuppressWarnings("unused")
    @FXML
    private Label activeTasksLabel;
    @SuppressWarnings("unused")
    @FXML
    private Label completedTasksLabel;
    @SuppressWarnings("unused")
    @FXML
    private Label overdueTasksLabel;

    @SuppressWarnings("unused")
    @FXML
    private ListView<Note> recentNotesList;
    @SuppressWarnings("unused")
    @FXML
    private ListView<TaskItem> upcomingTasksList;

    @FXML
    public void initialize() {
        populateLists();
        updateStats();
        // bind center container height to the scrollpane viewport so white panel fills visible area
        Platform.runLater(() -> {
            if (mainScroll != null && centerContainer != null) {
                mainScroll.viewportBoundsProperty().addListener((obs, oldB, newB) -> {
                    centerContainer.setMinHeight(newB.getHeight());
                    centerContainer.setPrefHeight(newB.getHeight());
                    centerContainer.setMaxHeight(Double.MAX_VALUE);
                });
                // set initial if available
                if (mainScroll.getViewportBounds() != null) {
                    double h = mainScroll.getViewportBounds().getHeight();
                    centerContainer.setMinHeight(h);
                    centerContainer.setPrefHeight(h);
                    centerContainer.setMaxHeight(Double.MAX_VALUE);
                }
            }
        });

        // simple cell factories for readable previews
        if (recentNotesList != null) {
            recentNotesList.setCellFactory(lv -> new ListCell<Note>() {
                @Override
                protected void updateItem(Note item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.getTitle() + " - " + (item.getContent().length() > 60 ? item.getContent().substring(0, 60) + "..." : item.getContent()));
                        // enforce readable text color/weight regardless of selection
                        setStyle("-fx-text-fill: #111111; -fx-font-weight: 400;");
                    }
                }
            });
            // open note viewer when user clicks an item
            recentNotesList.setOnMouseClicked(e -> {
                Note sel = recentNotesList.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    openModalWithController("/fxml/note_view.fxml", "View Note", ctrl -> {
                        try {
                            ((NoteViewController) ctrl).setNote(sel);
                        } catch (ClassCastException ex) {
                            // ignore if controller type doesn't match
                        }
                    });
                }
            });
        }
        if (upcomingTasksList != null) {
            upcomingTasksList.setCellFactory(lv -> new ListCell<TaskItem>() {
                @Override
                protected void updateItem(TaskItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.getTitle() + (item.getDueDate() != null ? " — due " + item.getDueDate().toLocalDate().toString() : ""));
                        // enforce readable text color/weight regardless of selection
                        setStyle("-fx-text-fill: #111111; -fx-font-weight: 400;");
                    }
                }
            });
            // open task viewer when user clicks an item
            upcomingTasksList.setOnMouseClicked(e -> {
                TaskItem sel = upcomingTasksList.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    openModalWithController("/fxml/task_view.fxml", "View Task", ctrl -> {
                        try {
                            ((TaskViewController) ctrl).setTask(sel);
                        } catch (ClassCastException ex) {
                            // ignore
                        }
                    });
                }
            });
        }
    }

    private void populateLists() {
        MockDataService svc = MockDataService.getInstance();
        System.out.println("[HomeController] service reports notes=" + svc.getAllNotes().size() + ", tasks=" + svc.getAllTasks().size());
        List<Note> notes = new ArrayList<>(svc.getAllNotes());
        List<TaskItem> tasks = new ArrayList<>(svc.getAllTasks());
        if (recentNotesList != null) {
            ObservableList<Note> asObs = FXCollections.observableArrayList();
            // show last 5 notes
            int start = Math.max(0, notes.size() - 5);
            for (int i = notes.size() - 1; i >= start; i--) asObs.add(notes.get(i));
            recentNotesList.setItems(asObs);
        }
        if (upcomingTasksList != null) {
            ObservableList<TaskItem> asObs = FXCollections.observableArrayList();
            LocalDateTime now = LocalDateTime.now();
            // show next 5 upcoming (not completed) sorted by due date
            tasks.stream()
                    .filter(t -> !t.isCompleted() && t.getDueDate() != null && t.getDueDate().isAfter(now))
                    .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                    .limit(5)
                    .forEach(asObs::add);
            upcomingTasksList.setItems(asObs);
        }
    }

    private void updateStats() {
        MockDataService svc = MockDataService.getInstance();
        int totalNotes = svc.getAllNotes().size();
        int completed = 0;
        int active = 0;
        int overdue = 0;
        for (TaskItem t : svc.getAllTasks()) {
            if (t.isCompleted()) completed++; else active++;
            if (!t.isCompleted() && t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())) overdue++;
        }
        if (totalNotesLabel != null) totalNotesLabel.setText(String.valueOf(totalNotes));
        if (activeTasksLabel != null) activeTasksLabel.setText(String.valueOf(active));
        if (completedTasksLabel != null) completedTasksLabel.setText(String.valueOf(completed));
        if (overdueTasksLabel != null) overdueTasksLabel.setText(String.valueOf(overdue));

        // refresh lists
        populateLists();
    }

    // open the New Note dialog
    @FXML
    private void onNewNoteClicked() {
        openModal("/fxml/notes_new.fxml", "New Note");
        updateStats();
    }

    // open the Add Task dialog
    @FXML
    private void onAddTaskClicked() {
        openModal("/fxml/tasks_new.fxml", "Add Task");
        updateStats();
    }

    // open the Set Reminders dialog
    @FXML
    private void onSetRemindersClicked() {
        openModal("/fxml/notifications_new.fxml", "Set Reminder");
        updateStats();
    }

    private void openModal(String fxmlPath, String title) {
        try {
            java.net.URL res = null;
            // prefer source copy so IDE edits show without rebuild
            java.io.File srcFile = new java.io.File("src/fxml" + fxmlPath.substring(fxmlPath.lastIndexOf('/')));
            if (srcFile.exists()) res = srcFile.toURI().toURL();
            if (res == null) res = getClass().getResource(fxmlPath);
            if (res == null) {
                java.io.File f = new java.io.File("out/production/Study Sphere/fxml" + fxmlPath.substring(fxmlPath.lastIndexOf('/')));
                if (f.exists()) res = f.toURI().toURL();
            }
            if (res == null) return;
            System.out.println("[HomeController] opening modal FXML from: " + res.toString());
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();
            // if the controller supports focus request, call it before showing
            Object ctrl = loader.getController();

            // create dialog stage
            Stage dialog = new Stage();
            // set owner if possible so dialog centers over app
            if (centerContainer != null && centerContainer.getScene() != null && centerContainer.getScene().getWindow() != null) {
                dialog.initOwner(centerContainer.getScene().getWindow());
                dialog.initModality(Modality.WINDOW_MODAL);
            } else {
                dialog.initModality(Modality.APPLICATION_MODAL);
            }
            // remove OS chrome so our X button inside the dialog is used
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            // wrap root in a Group so the Scene will size to the node's preferred size
            javafx.scene.Group wrapper = new javafx.scene.Group(root);
            Scene scene = new Scene(wrapper);
            // make the scene background transparent so only the dialog panel shows
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            // attach main stylesheet so modal uses the same visuals
            java.net.URL css = getClass().getResource("/fxml/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            dialog.setScene(scene);
            dialog.setResizable(false);
            // ensure size matches the dialog content
            dialog.sizeToScene();

            // when dialog is shown, center it and request focus from the controller (if provided)
            dialog.setOnShown(evt -> {
                // center over owner
                if (dialog.getOwner() != null) {
                    dialog.setX(dialog.getOwner().getX() + (dialog.getOwner().getWidth() - dialog.getWidth()) / 2);
                    dialog.setY(dialog.getOwner().getY() + (dialog.getOwner().getHeight() - dialog.getHeight()) / 2);
                }
                // call focus method on controller if available
                try {
                    if (ctrl != null) {
                        java.lang.reflect.Method m = ctrl.getClass().getMethod("requestInitialFocus");
                        if (m != null) m.invoke(ctrl);
                    }
                } catch (Exception ignore) {
                    // ignore any reflection errors
                }
            });

            // show and wait
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // reusable modal loader that allows initializing the controller instance
    private void openModalWithController(String fxmlPath, String title, Consumer<Object> initController) {
        try {
            java.net.URL res = null;
            java.io.File srcFile = new java.io.File("src/fxml" + fxmlPath.substring(fxmlPath.lastIndexOf('/')));
            if (srcFile.exists()) res = srcFile.toURI().toURL();
            if (res == null) res = getClass().getResource(fxmlPath);
            if (res == null) {
                java.io.File f = new java.io.File("out/production/Study Sphere/fxml" + fxmlPath.substring(fxmlPath.lastIndexOf('/')));
                if (f.exists()) res = f.toURI().toURL();
            }
            if (res == null) return;
            System.out.println("[HomeController] opening modal FXML from: " + res.toString());
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();
            Object ctrl = loader.getController();

            // allow caller to initialize controller with model
            if (initController != null) {
                try { initController.accept(ctrl); } catch (Exception ex) { ex.printStackTrace(); }
            }

            Stage dialog = new Stage();
            if (centerContainer != null && centerContainer.getScene() != null && centerContainer.getScene().getWindow() != null) {
                dialog.initOwner(centerContainer.getScene().getWindow());
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
                // if controller has initial focus hook, call it
                try {
                    if (ctrl != null) {
                        java.lang.reflect.Method m = ctrl.getClass().getMethod("requestInitialFocus");
                        if (m != null) m.invoke(ctrl);
                    }
                } catch (Exception ignore) { }
            });
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
