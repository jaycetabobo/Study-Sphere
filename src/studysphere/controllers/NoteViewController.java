package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import studysphere.models.Note;

public class NoteViewController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextArea contentArea;
    @FXML private Label subjectLabel;
    @FXML private Label tagsLabel;

    private Note note;

    public void setNote(Note note) {
        this.note = note;
        if (titleLabel != null) titleLabel.setText(note.getTitle());
        if (contentArea != null) contentArea.setText(note.getContent());
        if (subtitleLabel != null) subtitleLabel.setText("Created: " + (note.getCreated() != null ? note.getCreated().toLocalDate().toString() : ""));
        // subject = first tag if available, tags = comma separated
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            if (subjectLabel != null) subjectLabel.setText(note.getTags().get(0));
            if (tagsLabel != null) tagsLabel.setText(String.join(", ", note.getTags()));
        } else {
            if (subjectLabel != null) subjectLabel.setText("");
            if (tagsLabel != null) tagsLabel.setText("");
        }
    }

    @FXML
    private void onClose() {
        Stage s = (Stage) titleLabel.getScene().getWindow();
        s.close();
    }

    @FXML
    private void onDelete() {
        if (note == null) return;
        // remove from data service
        studysphere.services.MockDataService.getInstance().removeNote(note.getId());
        // if owner window has the notes ListView, remove the item from it so UI updates immediately
        try {
            Stage s = (Stage) titleLabel.getScene().getWindow();
            if (s != null && s.getOwner() != null && s.getOwner().getScene() != null) {
                javafx.scene.Scene ownerScene = s.getOwner().getScene();
                javafx.scene.control.ListView ownerList = (javafx.scene.control.ListView) ownerScene.lookup("#notesList");
                if (ownerList != null) {
                    ownerList.getItems().removeIf(i -> {
                        try {
                            if (i instanceof studysphere.models.Note) return ((studysphere.models.Note) i).getId().equals(note.getId());
                        } catch (Exception ignore) {}
                        return false;
                    });
                }
            }
        } catch (Exception ignore) {}
        // close dialog
        Stage s = (Stage) titleLabel.getScene().getWindow();
        s.close();
    }
}
