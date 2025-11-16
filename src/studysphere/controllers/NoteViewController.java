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
}
