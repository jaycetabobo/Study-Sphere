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
    @FXML private javafx.scene.control.TextField titleField;
    @FXML private javafx.scene.control.TextField tagsField;
    @FXML private javafx.scene.control.TextField subjectField;
    @FXML private javafx.scene.control.Button editButton;

    private Note note;
    private boolean editing = false;

    public void setNote(Note note) {
        this.note = note;
        if (titleLabel != null) titleLabel.setText(note.getTitle());
        if (titleField != null) titleField.setText(note.getTitle());
        if (contentArea != null) contentArea.setText(note.getContent());
        if (subjectField != null) subjectField.setText(note.getTags() != null && !note.getTags().isEmpty() ? note.getTags().get(0) : "");
        if (tagsField != null) tagsField.setText(String.join(",", note.getTags()));
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
    private void onEditToggle() {
        editing = !editing;
        if (editing) {
            // enter edit mode
            if (titleLabel != null) titleLabel.setVisible(false);
            if (titleField != null) titleField.setVisible(true);
            if (subjectLabel != null) subjectLabel.setVisible(false);
            if (subjectField != null) subjectField.setVisible(true);
            if (tagsLabel != null) tagsLabel.setVisible(false);
            if (tagsField != null) tagsField.setVisible(true);
            if (contentArea != null) contentArea.setEditable(true);
            if (editButton != null) editButton.setText("Save");
        } else {
            // save changes
            if (note != null) {
                if (titleField != null) note.setTitle(titleField.getText());
                if (contentArea != null) note.setContent(contentArea.getText());
                // subject becomes the first tag (if provided), then append other tags
                java.util.List<String> newTags = new java.util.ArrayList<>();
                if (subjectField != null && subjectField.getText() != null && !subjectField.getText().isBlank()) {
                    newTags.add(subjectField.getText().trim());
                }
                if (tagsField != null && tagsField.getText() != null && !tagsField.getText().isBlank()){
                    String subjVal = (subjectField != null && subjectField.getText() != null) ? subjectField.getText().trim() : "";
                    String[] parts = tagsField.getText().split(",");
                    for (String p: parts) {
                        String t = p.trim();
                        if (t.isEmpty()) continue;
                        if (!subjVal.isEmpty() && t.equalsIgnoreCase(subjVal)) continue; // avoid duplicate subject
                        newTags.add(t);
                    }
                }
                note.getTags().clear();
                note.getTags().addAll(newTags);
                studysphere.services.MockDataService.getInstance().addNote(note);
                // update owner notes list if present
                try {
                    Stage s = (Stage) titleLabel.getScene().getWindow();
                    if (s != null && s.getOwner() != null && s.getOwner().getScene() != null) {
                        javafx.scene.Scene ownerScene = s.getOwner().getScene();
                        Object node = ownerScene.lookup("#notesList");
                        if (node instanceof javafx.scene.control.ListView) {
                            @SuppressWarnings("unchecked")
                            javafx.scene.control.ListView<studysphere.models.Note> ownerList = (javafx.scene.control.ListView<studysphere.models.Note>) node;
                            // replace the item in the list with updated note at same index if present
                            int found = -1;
                            for (int j = 0; j < ownerList.getItems().size(); j++) {
                                studysphere.models.Note it = ownerList.getItems().get(j);
                                if (it != null && it.getId().equals(note.getId())) { found = j; break; }
                            }
                            if (found >= 0) ownerList.getItems().set(found, note); else ownerList.getItems().add(note);
                        }
                    }
                } catch (Exception ignore) {}
            }
            // exit edit mode
            if (titleLabel != null) titleLabel.setVisible(true);
            if (titleField != null) titleField.setVisible(false);
            if (subjectLabel != null) subjectLabel.setVisible(true);
            if (subjectField != null) subjectField.setVisible(false);
            if (tagsLabel != null) tagsLabel.setVisible(true);
            if (tagsField != null) tagsField.setVisible(false);
            if (contentArea != null) contentArea.setEditable(false);
            if (editButton != null) editButton.setText("Edit");
            // refresh displayed labels
            if (titleLabel != null && note != null) titleLabel.setText(note.getTitle());
            if (subjectLabel != null && note != null) subjectLabel.setText(note.getTags() != null && !note.getTags().isEmpty() ? note.getTags().get(0) : "");
            if (tagsLabel != null && note != null) tagsLabel.setText(String.join(", ", note.getTags()));
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
