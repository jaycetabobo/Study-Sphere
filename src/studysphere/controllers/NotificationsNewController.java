package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import studysphere.services.MockDataService;
import studysphere.models.Reminder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class NotificationsNewController {
    @FXML
    private TextField reminderTitle;

    @FXML
    private ComboBox<String> reminderType;

    @FXML
    private DatePicker reminderDate;

    @FXML
    private ComboBox<String> reminderHour;

    @FXML
    private ComboBox<String> reminderMinute;

    @FXML
    private ComboBox<String> reminderAmPm;

    @FXML
    private TextArea reminderMessage;

    // request focus hook used by modal
    public void requestInitialFocus() {
        if (reminderTitle != null) reminderTitle.requestFocus();
    }

    @FXML
    void initialize() {
        if (reminderType != null) {
            reminderType.getItems().addAll("Custom", "Work", "Personal");
            reminderType.getSelectionModel().selectFirst();
            // Explicit renderer so label and popup items are visible and consistent with CSS
            reminderType.setCellFactory(cb -> new ListCell<String>(){
                @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item); }
            });
            reminderType.setButtonCell(new ListCell<String>(){ @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item); } });
        }
        if (reminderDate != null && reminderDate.getEditor() != null) {
            reminderDate.getEditor().setPromptText("dd/mm/yyyy");
        }
        // populate time pickers
        if (reminderHour != null) {
            for (int h = 1; h <= 12; h++) reminderHour.getItems().add(String.format("%02d", h));
            reminderHour.getSelectionModel().select(0);
            // Ensure selected value is shown reliably
            reminderHour.setCellFactory(cb -> {
                ListCell<String> cell = new ListCell<String>(){
                    @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item); setGraphic(null); }
                };
                cell.setOnMouseClicked(evt -> { if (!cell.isEmpty()){ reminderHour.getSelectionModel().select(cell.getItem()); reminderHour.hide(); } });
                return cell;
            });
            ListCell<String> hourButtonCell = new ListCell<>(){ @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item); setGraphic(null); } };
            hourButtonCell.setAlignment(Pos.CENTER_LEFT);
            hourButtonCell.setPrefWidth(160);
            // use a Label graphic so text isn't clipped by cell rendering
            Label hourLabel = new Label();
            hourLabel.setPrefWidth(160);
            hourLabel.setMaxWidth(160);
            hourLabel.setWrapText(false);
            hourButtonCell.setGraphic(hourLabel);
            // ensure cell text is not used (we rely on graphic)
            hourButtonCell.setText(null);
            reminderHour.setButtonCell(hourButtonCell);
            reminderHour.valueProperty().addListener((obs,oldV,newV)->{ hourLabel.setText(newV==null?"":newV); });
            // set initial displayed text
            hourLabel.setText(reminderHour.getValue()!=null?reminderHour.getValue():"");
         }
         if (reminderMinute != null) {
             for (int m = 0; m < 60; m += 5) reminderMinute.getItems().add(String.format("%02d", m));
             reminderMinute.getSelectionModel().select(0);
             reminderMinute.setCellFactory(cb -> {
                ListCell<String> cell = new ListCell<String>(){
                    @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item); setGraphic(null); }
                };
                cell.setOnMouseClicked(evt -> { if (!cell.isEmpty()){ reminderMinute.getSelectionModel().select(cell.getItem()); reminderMinute.hide(); } });
                return cell;
            });
             Label minLabel = new Label(); minLabel.setPrefWidth(160); minLabel.setStyle("-fx-text-fill:#111111; -fx-padding:0 6 0 6;"); minLabel.setAlignment(Pos.CENTER_LEFT);
             ListCell<String> minButtonCell = new ListCell<>(){ @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(null); setGraphic(minLabel); } };
             minButtonCell.setAlignment(Pos.CENTER_LEFT);
             minButtonCell.setPrefWidth(160);
             minButtonCell.setGraphic(minLabel);
             reminderMinute.setButtonCell(minButtonCell);
             reminderMinute.valueProperty().addListener((obs,oldV,newV)->{ minLabel.setText(newV==null?"":newV); });
             minLabel.setText(reminderMinute.getValue()!=null?reminderMinute.getValue():"");
         }
         if (reminderAmPm != null) {
             reminderAmPm.getItems().addAll("AM", "PM");
             reminderAmPm.getSelectionModel().select(0);
             reminderAmPm.setCellFactory(cb -> {
                ListCell<String> cell = new ListCell<String>(){
                    @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item); setGraphic(null); }
                };
                cell.setOnMouseClicked(evt -> { if (!cell.isEmpty()){ reminderAmPm.getSelectionModel().select(cell.getItem()); reminderAmPm.hide(); } });
                return cell;
            });
             Label ampmLabel = new Label(); ampmLabel.setPrefWidth(120); ampmLabel.setStyle("-fx-text-fill:#111111; -fx-padding:0 6 0 6;"); ampmLabel.setAlignment(Pos.CENTER_LEFT);
             ListCell<String> ampmButtonCell = new ListCell<>(){ @Override protected void updateItem(String item, boolean empty){ super.updateItem(item, empty); setText(null); setGraphic(ampmLabel); } };
             ampmButtonCell.setAlignment(Pos.CENTER_LEFT);
             ampmButtonCell.setPrefWidth(120);
             ampmButtonCell.setGraphic(ampmLabel);
             reminderAmPm.setButtonCell(ampmButtonCell);
             reminderAmPm.valueProperty().addListener((obs,oldV,newV)->{ ampmLabel.setText(newV==null?"":newV); });
             ampmLabel.setText(reminderAmPm.getValue()!=null?reminderAmPm.getValue():"");
         }
    }

    @FXML
    void onSaveNewReminder() {
        String title = reminderTitle != null ? reminderTitle.getText() : "";
        String type = reminderType != null && reminderType.getValue() != null ? reminderType.getValue() : "Custom";
        LocalDate date = reminderDate != null ? reminderDate.getValue() : null;
        String hour = reminderHour != null && reminderHour.getValue() != null ? reminderHour.getValue() : "";
        String minute = reminderMinute != null && reminderMinute.getValue() != null ? reminderMinute.getValue() : "";
        String ampm = reminderAmPm != null && reminderAmPm.getValue() != null ? reminderAmPm.getValue() : "";
        String time = (hour.isBlank() ? "" : hour + ":" + minute + " " + ampm).trim();
        String message = reminderMessage != null ? reminderMessage.getText() : "";

        String dateStr = date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

        Reminder r = new Reminder(UUID.randomUUID().toString(), title, type, dateStr, time, message);
        MockDataService.getInstance().addReminder(r);

        if (reminderTitle != null) {
            Stage s = (Stage) reminderTitle.getScene().getWindow();
            s.close();
        }
    }

    @FXML
    void onCancelNewReminder() {
        if (reminderTitle != null) {
            Stage s = (Stage) reminderTitle.getScene().getWindow();
            s.close();
        }
    }
}
