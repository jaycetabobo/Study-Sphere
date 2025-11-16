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
    @FXML private javafx.scene.control.TextField taskTitleField;
    @FXML private javafx.scene.control.TextField taskSubjectField;
    @FXML private javafx.scene.control.DatePicker taskDuePicker;
    @FXML private javafx.scene.control.ChoiceBox<String> taskPriorityChoice;
    @FXML private javafx.scene.control.ComboBox<String> taskStatusCombo;
    @FXML private javafx.scene.control.Button editTaskButton;
    @FXML private javafx.scene.control.Button cancelTaskButton;

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
    // currently selected task in the details pane
    private TaskItem task;
    private static final java.time.format.DateTimeFormatter DATE_FMT = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                    // track current task so edit/save works without extra lookups
                    this.task = newV;
                    taskTitle.setText(newV.getTitle());
                    taskDue.setText(newV.getDueDate() == null ? "" : newV.getDueDate().toLocalDate().toString());
                    taskPriority.setText(newV.getPriority());
                    taskSubjectLabel.setText(newV.getSubject() == null ? "" : newV.getSubject());
                    taskDescriptionArea.setText(newV.getDescription() == null ? "" : newV.getDescription());
                    taskCompleted.setSelected(newV.isCompleted());
                    // keep datepicker value in sync (hidden by default)
                    if (taskDuePicker != null) {
                        taskDuePicker.setValue(newV.getDueDate() == null ? null : newV.getDueDate().toLocalDate());
                        taskDuePicker.setVisible(false);
                        taskDuePicker.setDisable(false);
                        taskDuePicker.setManaged(false);
                    }
                } else {
                    this.task = null;
                    taskTitle.setText("");
                    taskDue.setText("");
                    taskPriority.setText("");
                    taskSubjectLabel.setText("");
                    taskDescriptionArea.setText("");
                    taskCompleted.setSelected(false);
                    if (taskDuePicker != null) {
                        taskDuePicker.setValue(null);
                        taskDuePicker.setVisible(false);
                        taskDuePicker.setManaged(false);
                    }
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
        // ensure the inline task Due DatePicker (details pane) has a prompt and is hidden by default
        if (taskDuePicker != null) {
            if (taskDuePicker.getEditor() != null) taskDuePicker.getEditor().setPromptText("dd/mm/yyyy");
            taskDuePicker.setVisible(false);
            taskDuePicker.setDisable(true);
            taskDuePicker.setManaged(false);
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

    @FXML
    private void onEditToggleTask() {
        if (task == null) {
            TaskItem sel = taskList.getSelectionModel().getSelectedItem();
            if (sel == null) return; else task = sel;
        }
        boolean entering = editTaskButton != null && "Edit".equals(editTaskButton.getText());
        if (entering) {
            // enter edit mode
            if (taskTitle != null) taskTitle.setVisible(false);
            if (taskTitleField != null) { taskTitleField.setVisible(true); taskTitleField.setText(task.getTitle()); taskTitleField.setManaged(true);}
            if (taskSubjectLabel != null) taskSubjectLabel.setVisible(false);
            if (taskSubjectField != null) { taskSubjectField.setVisible(true); taskSubjectField.setText(task.getSubject()); taskSubjectField.setManaged(true);}
            if (taskDescriptionArea != null) taskDescriptionArea.setEditable(true);
            // always show the date picker when entering edit mode; populate it if a due date exists
            if (taskDuePicker != null) {
                taskDuePicker.setVisible(true);
                taskDuePicker.setDisable(false);
                taskDuePicker.setManaged(true);
                taskDuePicker.setValue(task.getDueDate() != null ? task.getDueDate().toLocalDate() : null);
                try { taskDuePicker.requestFocus(); } catch (Exception ignore) {}
                try { taskDuePicker.show(); } catch (Exception ignore) {}
            }
            if (taskPriorityChoice != null) { taskPriorityChoice.setVisible(true); taskPriorityChoice.setItems(FXCollections.observableArrayList("High","Medium","Low")); taskPriorityChoice.setValue(task.getPriority()); taskPriorityChoice.setManaged(true);}
            if (taskStatusCombo != null) { taskStatusCombo.setVisible(true); taskStatusCombo.setItems(FXCollections.observableArrayList("To-do","In Progress","Done","Archive")); taskStatusCombo.setValue(task.getStatus()); taskStatusCombo.setManaged(true);}
            if (editTaskButton != null) editTaskButton.setText("Save");
            if (cancelTaskButton != null) { cancelTaskButton.setVisible(true); cancelTaskButton.setManaged(true); }
        } else {
            // save changes
            if (taskTitleField != null) task.setTitle(taskTitleField.getText());
            if (taskDescriptionArea != null) task.setDescription(taskDescriptionArea.getText());
            if (taskSubjectField != null) task.setSubject(taskSubjectField.getText());
            if (taskDuePicker != null && taskDuePicker.getValue() != null) task.setDueDate(taskDuePicker.getValue().atStartOfDay());
            if (taskPriorityChoice != null && taskPriorityChoice.getValue() != null) task.setPriority(taskPriorityChoice.getValue());
            if (taskStatusCombo != null && taskStatusCombo.getValue() != null) task.setStatus(taskStatusCombo.getValue());
            // persist
            MockDataService.getInstance().addTask(task);
            // update list in-place
            if (taskList != null) {
                int found=-1; for (int i=0;i<taskList.getItems().size();i++){ if (taskList.getItems().get(i).getId().equals(task.getId())) { found=i; break; } }
                if (found>=0) taskList.getItems().set(found, task);
            }
            // exit edit mode
            if (taskTitle != null) taskTitle.setVisible(true);
            if (taskTitleField != null) { taskTitleField.setVisible(false); taskTitleField.setManaged(false); }
            if (taskSubjectLabel != null) taskSubjectLabel.setVisible(true);
            if (taskSubjectField != null) { taskSubjectField.setVisible(false); taskSubjectField.setManaged(false); }
            if (taskDescriptionArea != null) taskDescriptionArea.setEditable(false);
            if (taskDuePicker != null) { taskDuePicker.setVisible(false); taskDuePicker.setManaged(false); taskDuePicker.setDisable(true); }
            if (taskPriorityChoice != null) { taskPriorityChoice.setVisible(false); taskPriorityChoice.setManaged(false); }
            if (taskStatusCombo != null) { taskStatusCombo.setVisible(false); taskStatusCombo.setManaged(false); }
            if (editTaskButton != null) editTaskButton.setText("Edit");
            if (cancelTaskButton != null) { cancelTaskButton.setVisible(false); cancelTaskButton.setManaged(false); }
             // refresh labels
             if (taskTitle != null) taskTitle.setText(task.getTitle());
             if (taskDue != null) taskDue.setText(task.getDueDate() == null ? "" : task.getDueDate().toLocalDate().format(DATE_FMT));
             if (taskPriority != null) taskPriority.setText(task.getPriority());
             if (taskSubjectLabel != null) taskSubjectLabel.setText(task.getSubject());
         }
     }

    @FXML
    private void onCancelEditTask() {
        // discard changes and restore the view state for the current task
        TaskItem sel = (task != null) ? task : (taskList != null ? taskList.getSelectionModel().getSelectedItem() : null);
        if (sel == null) return;
        // restore labels
        if (taskTitle != null) taskTitle.setText(sel.getTitle());
        if (taskDue != null) taskDue.setText(sel.getDueDate() == null ? "" : sel.getDueDate().toLocalDate().format(DATE_FMT));
        if (taskPriority != null) taskPriority.setText(sel.getPriority());
        if (taskSubjectLabel != null) taskSubjectLabel.setText(sel.getSubject());
        if (taskDescriptionArea != null) taskDescriptionArea.setText(sel.getDescription() == null ? "" : sel.getDescription());
        // hide editable controls
        if (taskTitleField != null) { taskTitleField.setVisible(false); taskTitleField.setManaged(false); }
        if (taskSubjectField != null) { taskSubjectField.setVisible(false); taskSubjectField.setManaged(false); }
        if (taskDuePicker != null) { taskDuePicker.setVisible(false); taskDuePicker.setManaged(false); taskDuePicker.setDisable(true); }
        if (taskPriorityChoice != null) { taskPriorityChoice.setVisible(false); taskPriorityChoice.setManaged(false); }
        if (taskStatusCombo != null) { taskStatusCombo.setVisible(false); taskStatusCombo.setManaged(false); }
        if (taskDescriptionArea != null) taskDescriptionArea.setEditable(false);
        // reset buttons
        if (editTaskButton != null) editTaskButton.setText("Edit");
        if (cancelTaskButton != null) { cancelTaskButton.setVisible(false); cancelTaskButton.setManaged(false); }
        // clear any temporary edits by re-syncing fields
        if (taskTitleField != null) taskTitleField.setText(sel.getTitle());
        if (taskSubjectField != null) taskSubjectField.setText(sel.getSubject());
        if (taskDuePicker != null) taskDuePicker.setValue(sel.getDueDate() == null ? null : sel.getDueDate().toLocalDate());
        if (taskPriorityChoice != null) taskPriorityChoice.setValue(sel.getPriority());
        if (taskStatusCombo != null) taskStatusCombo.setValue(sel.getStatus());
        // leave `task` reference pointing to selected item
    }
}
