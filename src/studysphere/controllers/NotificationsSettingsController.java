package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class NotificationsSettingsController {
    @FXML private CheckBox enablePopups;
    @FXML private CheckBox enableSounds;
    @FXML private CheckBox enableExamReminders;
    @FXML private ComboBox<String> defaultReminderTime;

    @FXML
    public void initialize(){
        if (defaultReminderTime != null) {
            defaultReminderTime.getItems().addAll("1 hour","6 hours","12 hours","24 hours","48 hours");
            defaultReminderTime.getSelectionModel().select("24 hours");
        }
    }


    @FXML
    void onClose(){
        Stage s = (Stage) (defaultReminderTime != null ? defaultReminderTime.getScene().getWindow() : null);
        if (s != null) s.close();
    }
}
