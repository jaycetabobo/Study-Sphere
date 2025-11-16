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
    @FXML private javafx.scene.control.TextField titleField;
    @FXML private javafx.scene.control.TextField typeField;
    @FXML private javafx.scene.control.TextField dateField;
    @FXML private javafx.scene.control.TextField timeField;
    @FXML private javafx.scene.control.Button editButton;

    private Reminder reminder;
    private boolean editing = false;

    public void setReminder(Reminder r) {
        this.reminder = r;
        if (titleLabel != null) titleLabel.setText(r.getTitle() != null ? r.getTitle() : "Reminder");
        if (titleField != null) titleField.setText(r.getTitle() != null ? r.getTitle() : "");
        if (subtitleLabel != null) subtitleLabel.setText(r.getType() != null ? r.getType() : "");
        if (typeLabel != null) typeLabel.setText(r.getType() != null ? r.getType() : "");
        if (typeField != null) typeField.setText(r.getType() != null ? r.getType() : "");
        if (dateLabel != null) dateLabel.setText(r.getDate() != null ? r.getDate() : "");
        if (dateField != null) dateField.setText(r.getDate() != null ? r.getDate() : "");
        if (timeLabel != null) timeLabel.setText(r.getTime() != null ? r.getTime() : "");
        if (timeField != null) timeField.setText(r.getTime() != null ? r.getTime() : "");
        if (messageArea != null) messageArea.setText(r.getMessage() != null ? r.getMessage() : "");
    }

    @FXML
    private void onEditToggle() {
        editing = !editing;
        if (editing) {
            // enter edit mode
            if (titleLabel != null) titleLabel.setVisible(false);
            if (titleField != null) titleField.setVisible(true);
            if (typeLabel != null) typeLabel.setVisible(false);
            if (typeField != null) typeField.setVisible(true);
            if (dateLabel != null) dateLabel.setVisible(false);
            if (dateField != null) dateField.setVisible(true);
            if (timeLabel != null) timeLabel.setVisible(false);
            if (timeField != null) timeField.setVisible(true);
            if (messageArea != null) messageArea.setEditable(true);
            if (editButton != null) editButton.setText("Save");
        } else {
            // save changes
            if (reminder != null) {
                if (titleField != null) reminder.setTitle(titleField.getText());
                if (typeField != null) reminder.setType(typeField.getText());
                if (dateField != null) reminder.setDate(dateField.getText());
                if (timeField != null) reminder.setTime(timeField.getText());
                if (messageArea != null) reminder.setMessage(messageArea.getText());
                // persist: remove by id then add updated
                try { MockDataService.getInstance().removeReminder(reminder.getId()); } catch (Exception ignore) {}
                MockDataService.getInstance().addReminder(reminder);
                // update owner list in-place
                try {
                    Stage s = (Stage) titleLabel.getScene().getWindow();
                    if (s != null && s.getOwner() != null && s.getOwner().getScene() != null) {
                        javafx.scene.Scene ownerScene = s.getOwner().getScene();
                        Object node = ownerScene.lookup("#reminderList");
                        if (node instanceof javafx.scene.control.ListView) {
                            @SuppressWarnings("unchecked")
                            javafx.scene.control.ListView<studysphere.models.Reminder> ownerList = (javafx.scene.control.ListView<studysphere.models.Reminder>) node;
                            int found=-1;
                            for (int i=0;i<ownerList.getItems().size();i++){
                                studysphere.models.Reminder it = ownerList.getItems().get(i);
                                if (it!=null && it.getId().equals(reminder.getId())) { found=i; break; }
                            }
                            if (found>=0) ownerList.getItems().set(found, reminder);
                        }
                    }
                } catch (Exception ignore) {}
            }
            // exit edit mode
            if (titleLabel != null) titleLabel.setVisible(true);
            if (titleField != null) titleField.setVisible(false);
            if (typeLabel != null) typeLabel.setVisible(true);
            if (typeField != null) typeField.setVisible(false);
            if (dateLabel != null) dateLabel.setVisible(true);
            if (dateField != null) dateField.setVisible(false);
            if (timeLabel != null) timeLabel.setVisible(true);
            if (timeField != null) timeField.setVisible(false);
            if (messageArea != null) messageArea.setEditable(false);
            if (editButton != null) editButton.setText("Edit");
            // refresh labels
            if (titleLabel != null && reminder != null) titleLabel.setText(reminder.getTitle());
            if (typeLabel != null && reminder != null) typeLabel.setText(reminder.getType());
            if (dateLabel != null && reminder != null) dateLabel.setText(reminder.getDate());
            if (timeLabel != null && reminder != null) timeLabel.setText(reminder.getTime());
        }
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
