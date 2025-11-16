package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import studysphere.models.TaskItem;

import java.time.format.DateTimeFormatter;

public class TaskViewController {
    @FXML private Label taskTitleLabel;
    @FXML private Label taskSubtitleLabel;
    @FXML private TextArea taskDetailsArea;
    @FXML private Label dueLabel;
    @FXML private Label statusLabel;
    @FXML private Label subjectLabel;

    private TaskItem task;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setTask(TaskItem task) {
        this.task = task;
        if (taskTitleLabel != null) taskTitleLabel.setText(task.getTitle());
        // show description in details area
        if (taskDetailsArea != null) taskDetailsArea.setText(task.getDescription() == null ? "" : task.getDescription());
        if (dueLabel != null) dueLabel.setText(task.getDueDate() == null ? "" : task.getDueDate().toLocalDate().format(DATE_FMT));
        if (statusLabel != null) statusLabel.setText(task.getStatus() == null ? (task.isCompleted() ? "Completed" : "Active") : task.getStatus());
        if (subjectLabel != null) subjectLabel.setText(task.getSubject() == null ? "" : task.getSubject());
    }

    @FXML
    private void onClose() {
        Stage s = (Stage) taskTitleLabel.getScene().getWindow();
        s.close();
    }
}
