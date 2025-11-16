package studysphere.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private String id;
    private String title;
    private String content;
    private List<String> tags = new ArrayList<>();
    private LocalDateTime created;

    public Note(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.created = LocalDateTime.now();
    }

    // getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getTags() { return tags; }
    public LocalDateTime getCreated() { return created; }
}
