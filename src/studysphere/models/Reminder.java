package studysphere.models;

public class Reminder {
    private String id;
    private String title;
    private String type;
    private String date; // stored as dd/MM/yyyy or ISO depending on source
    private String time; // stored as text like HH:mm or with AM/PM
    private String message;

    public Reminder(String id, String title, String type, String date, String time, String message) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.date = date;
        this.time = time;
        this.message = message;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getMessage() { return message; }

    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type != null && !type.isEmpty()) sb.append("[").append(type).append("] ");
        if (title != null && !title.isEmpty()) sb.append(title).append(" - ");
        if (date != null && !date.isEmpty()) sb.append(date).append(" ");
        if (time != null && !time.isEmpty()) sb.append(time).append(" ");
        if (message != null && !message.isEmpty()) sb.append("- ").append(message);
        return sb.toString().trim();
    }
}

