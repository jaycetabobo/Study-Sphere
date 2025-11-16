package studysphere.controllers;

import javafx.application.Platform;
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
import studysphere.services.MockDataService;
import studysphere.models.TaskItem;
import studysphere.models.Reminder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class NotificationsController {
    @FXML private ImageView bgNotifications;
    @FXML private StackPane notificationsStack;

    @FXML private ListView<Reminder> reminderList;
    @FXML private VBox reminderEmpty;
    @FXML private Label upcomingCountLabel;
    @FXML private ListView<Reminder> upcomingList;
    @FXML private Label upcomingMessageLabel;
    @FXML private ToggleButton enableSounds;
    @FXML private ToggleButton enablePopups;

    // new reminder field (not used here; new-reminder dialog has its own controller)
    @FXML private TextField newReminderText;

    private ObservableList<Reminder> items = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<Reminder> upcomingFiltered;
    private Timer timer;

    @FXML
    public void initialize() {
        // bind bg if present
        if (bgNotifications != null && notificationsStack != null){
            bgNotifications.fitWidthProperty().bind(notificationsStack.widthProperty());
            bgNotifications.fitHeightProperty().bind(notificationsStack.heightProperty());
        }

        // populate reminders
        items.setAll(MockDataService.getInstance().getReminders());
        // filtered view for upcoming reminders (those with a date set OR legacy string messages containing a date)
        upcomingFiltered = new javafx.collections.transformation.FilteredList<>(items, r -> hasUpcomingDate(r));
        if (upcomingList != null) {
            upcomingList.setItems(upcomingFiltered);
            upcomingList.setCellFactory(lv -> new ListCell<Reminder>(){
                @Override protected void updateItem(Reminder r, boolean empty){
                    super.updateItem(r, empty);
                    if (empty || r==null){ setText(null); setGraphic(null); }
                    else {
                        // show structured date when present, otherwise show legacy message
                        if (r.getDate()!=null && !r.getDate().isBlank()){
                            String text = (r.getTitle()!=null?r.getTitle():"") + (r.getDate()!=null && !r.getDate().isBlank() ? " — "+r.getDate() : "") + (r.getTime()!=null && !r.getTime().isBlank() ? " "+r.getTime() : "");
                            setText(text);
                        } else if (r.getMessage() != null && !r.getMessage().isBlank()){
                            setText(r.getMessage());
                        } else {
                            setText(r.getTitle()!=null?r.getTitle():"");
                        }
                        setWrapText(true);
                    }
                }
            });
        }
        if (reminderList != null) {
            reminderList.setItems(items);
            reminderList.setCellFactory(lv -> new ListCell<Reminder>(){
                @Override
                protected void updateItem(Reminder r, boolean empty){
                    super.updateItem(r, empty);
                    if (empty || r == null){
                        setText(null);
                        setGraphic(null);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        if (r.getTitle() != null && !r.getTitle().isBlank()) sb.append(r.getTitle());
                        if (r.getDate() != null && !r.getDate().isBlank()) sb.append(" — ").append(r.getDate());
                        if (r.getTime() != null && !r.getTime().isBlank()) sb.append(" ").append(r.getTime());
                        if (r.getMessage() != null && !r.getMessage().isBlank()) sb.append("\n").append(r.getMessage());
                        setText(sb.toString());
                        setWrapText(true);
                    }
                }
            });
        }
        // manage empty placeholder visibility
        updateEmptyPlaceholder();
        updateUpcomingSummary();
        // listen for changes so placeholder toggles and upcoming summary updates
        items.addListener((javafx.collections.ListChangeListener.Change<? extends Reminder> c) -> Platform.runLater(() -> { updateEmptyPlaceholder(); updateUpcomingSummary(); }));

        // schedule timer to check task due dates every minute
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { checkDueTasks(); }
        }, 10_000, 60_000);
    }

    private void checkDueTasks(){
        // collect new reminders on this background thread, then apply to UI on FX thread
        List<Reminder> toAdd = new ArrayList<>();
        for (TaskItem t: MockDataService.getInstance().getAllTasks()){
            if (!t.isCompleted() && t.getDueDate() != null){
                Duration d = Duration.between(LocalDateTime.now(), t.getDueDate());
                long hours = d.toHours();
                if (hours >=0 && hours <=24){
                    String msg = t.getTitle() + " is due in " + hours + " hour(s).";
                    boolean exists = items.stream().anyMatch(r -> r.getMessage() != null && r.getMessage().contains(msg));
                    if (!exists){
                        Reminder rem = new Reminder(UUID.randomUUID().toString(), t.getTitle(), "Auto", t.getDueDate().toLocalDate().toString(), "", msg);
                        toAdd.add(rem);
                    }
                }
            }
        }
        if (!toAdd.isEmpty()){
            Platform.runLater(() -> {
                for (Reminder rem: toAdd){
                    items.add(0, rem);
                    MockDataService.getInstance().addReminder(rem);
                }
                updateEmptyPlaceholder();
                updateUpcomingSummary();
                // show popups / play sound if enabled
                for (Reminder rem: toAdd){
                    String msg = rem.getMessage();
                    if (enablePopups != null && enablePopups.isSelected()){
                        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
                        a.setHeaderText("Upcoming due");
                        a.show();
                    }
                    if (enableSounds != null && enableSounds.isSelected()){
                        java.awt.Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
        }
        // always ensure placeholder/summary updated in FX thread
        Platform.runLater(() -> { updateEmptyPlaceholder(); updateUpcomingSummary(); });
    }

    private void updateEmptyPlaceholder(){
        boolean empty = items.isEmpty();
        if (reminderEmpty != null) reminderEmpty.setVisible(empty);
        if (reminderList != null) reminderList.setVisible(!empty);
    }

    private void updateUpcomingSummary(){
        if (upcomingCountLabel == null || upcomingMessageLabel == null) return;
        long count = upcomingFiltered == null ? 0 : upcomingFiltered.size();
        upcomingCountLabel.setText("Your next " + count + " active reminders");
        if (count==0) {
            upcomingMessageLabel.setText("No upcoming reminders.");
            if (upcomingList != null) upcomingList.setVisible(false);
        } else {
            upcomingMessageLabel.setText("");
            if (upcomingList != null) upcomingList.setVisible(true);
        }
    }

    @FXML
    void onNewReminder() {
        try {
            java.net.URL res = null;
            // prefer source copy so IDE edits show without rebuild
            java.io.File srcFile = new java.io.File("src/fxml/notifications_new.fxml");
            if (srcFile.exists()) res = srcFile.toURI().toURL();
            if (res == null) res = getClass().getResource("/fxml/notifications_new.fxml");
            if (res == null) {
                java.io.File f = new java.io.File("out/production/Study Sphere/fxml/notifications_new.fxml");
                if (f.exists()) res = f.toURI().toURL();
            }
            if (res == null) return;
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();
            Object ctrl = loader.getController();

            Stage dialog = new Stage();
            if (reminderList != null && reminderList.getScene() != null && reminderList.getScene().getWindow() != null) {
                dialog.initOwner(reminderList.getScene().getWindow());
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
            // refresh
            items.setAll(MockDataService.getInstance().getReminders());
            Platform.runLater(this::updateEmptyPlaceholder);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void onSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/notifications_settings.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            if (reminderList != null && reminderList.getScene() != null && reminderList.getScene().getWindow() != null) dialog.initOwner(reminderList.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Notification Settings");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void onSaveSettings() {
        if (enableSounds != null) {
            Stage s = (Stage) enableSounds.getScene().getWindow();
            s.close();
        }
    }

    @FXML
    void onBackFromSettings() {
        if (enableSounds != null) {
            Stage s = (Stage) enableSounds.getScene().getWindow();
            s.close();
        }
    }

    // treat a reminder as upcoming if it has an explicit date OR its message contains a date-like pattern (legacy string-based reminders)
    private boolean hasUpcomingDate(Reminder r){
        if (r == null) return false;
        if (r.getDate() != null && !r.getDate().isBlank()) return true;
        String m = r.getMessage();
        if (m == null) return false;
        // match dd/mm/yyyy or yyyy-mm-dd
        return m.matches(".*(\\d{1,2}/\\d{1,2}/\\d{4}|\\d{4}-\\d{2}-\\d{2}).*");
    }
}
