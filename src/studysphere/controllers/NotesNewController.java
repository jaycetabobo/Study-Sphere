package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import studysphere.models.Note;
import studysphere.services.MockDataService;

import java.util.UUID;

public class NotesNewController {
    @FXML
    private TextField noteTitleNew;
    @FXML
    private TextField noteSubjectNew;
    @FXML
    private TextField noteTagsNew;
    @FXML
    private TextArea noteContentNew;

    @FXML
    private void onSaveNoteNew() {
        String t = noteTitleNew.getText();
        String c = noteContentNew.getText();
        String tags = noteTagsNew.getText();
        if (t != null && !t.isBlank()) {
            Note n = new Note(UUID.randomUUID().toString(), t, c);
            if (tags != null && !tags.isBlank()) {
                String[] parts = tags.split(",");
                for (String p : parts) n.getTags().add(p.trim());
            }
            if (noteSubjectNew != null && noteSubjectNew.getText() != null && !noteSubjectNew.getText().isBlank()) {
                n.getTags().add(0, noteSubjectNew.getText().trim());
            }
            MockDataService.getInstance().addNote(n);
            // close the dialog window
            Stage s = (Stage) noteTitleNew.getScene().getWindow();
            s.close();
        }
    }

    @FXML
    private void onCancelNewNote() {
        Stage s = (Stage) noteTitleNew.getScene().getWindow();
        s.close();
    }

    // called immediately after FXML load so modal can set initial focus
    public void requestInitialFocus() {
        if (noteTitleNew != null) {
            noteTitleNew.requestFocus();
        }
    }
}
