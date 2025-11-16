package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import studysphere.models.Reminder;
import studysphere.services.MockDataService;

public class ReminderViewController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private TextArea messageArea;

    private Reminder reminder;

    public void setReminder(Reminder r) {
        this.reminder = r;
        if (titleLabel != null) titleLabel.setText(r.getTitle() != null ? r.getTitle() : "Reminder");
        if (subtitleLabel != null) subtitleLabel.setText(r.getType() != null ? r.getType() : "");
        if (typeLabel != null) typeLabel.setText(r.getType() != null ? r.getType() : "");
        if (dateLabel != null) dateLabel.setText(r.getDate() != null ? r.getDate() : "");
        if (timeLabel != null) timeLabel.setText(r.getTime() != null ? r.getTime() : "");
        if (messageArea != null) messageArea.setText(r.getMessage() != null ? r.getMessage() : "");
    }

    @FXML
    private void onClose() {
        Stage s = (Stage) titleLabel.getScene().getWindow();
        s.close();
    }

    @FXML
    private void onDelete() {
        if (reminder == null) return;
        // remove from service by index (existing API removes by index)
        java.util.List<studysphere.models.Reminder> list = MockDataService.getInstance().getReminders();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (reminder.getId().equals(list.get(i).getId())) { idx = i; break; }
        }
        if (idx >= 0) MockDataService.getInstance().removeReminder(idx);
        // try to update owner list view if present
        try {
            Stage s = (Stage) titleLabel.getScene().getWindow();
            if (s != null && s.getOwner() != null && s.getOwner().getScene() != null) {
                javafx.scene.Scene ownerScene = s.getOwner().getScene();
                Object node = ownerScene.lookup("#reminderList");
                if (node instanceof javafx.scene.control.ListView) {
                    @SuppressWarnings("unchecked")
                    javafx.scene.control.ListView<studysphere.models.Reminder> ownerList = (javafx.scene.control.ListView<studysphere.models.Reminder>) node;
                    ownerList.getItems().removeIf(i -> i != null && reminder.getId().equals(i.getId()));
                }
            }
        } catch (Exception ignore) {}
        // close dialog
        Stage s2 = (Stage) titleLabel.getScene().getWindow();
        s2.close();
    }
}
