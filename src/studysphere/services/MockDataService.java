package studysphere.services;

import studysphere.models.Note;
import studysphere.models.TaskItem;
import studysphere.models.Reminder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;

public class MockDataService {
    private static MockDataService instance;
    private Map<String, Note> notes = new LinkedHashMap<>();
    private Map<String, TaskItem> tasks = new LinkedHashMap<>();
    private List<Reminder> reminders = new ArrayList<>();

    // use explicit working-directory-based path so runtime lookup is reliable
    private final File dataFile = new File(System.getProperty("user.dir"), "studysphere_data.json");
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private MockDataService() {
        // try load from disk
        System.out.println("[MockDataService] data file path: " + dataFile.getAbsolutePath());
        boolean loaded = loadFromDisk();
        if (!loaded) {
            // seed sample notes (5 examples) with title, subject (first tag), tags and content
            String[] subjects = new String[]{"Mathematics", "History", "Chemistry", "Biology", "Physics"};
            String[][] extraTags = new String[][]{
                {"exam","formulas"},
                {"lecture","summary"},
                {"lab","important"},
                {"revision","diagrams"},
                {"proofs","examples"}
            };
            for (int i = 0; i < 5; i++) {
                String subj = subjects[i % subjects.length];
                Note n = new Note(UUID.randomUUID().toString(),
                        subj + " — Quick Study Notes",
                        "A concise set of study notes for " + subj + ".\n\nContents:\n- Overview of key concepts\n- Worked examples\n- Important formulas and reminders"
                );
                // subject stored as first tag so existing UI logic can treat it as subject
                n.getTags().add(subj);
                // add a couple of extra tags (e.g., exam, important)
                for (String t : extraTags[i % extraTags.length]) n.getTags().add(t);
                notes.put(n.getId(), n);
            }

            // seed sample tasks (30 examples)
            String[] priorities = new String[]{"High", "Medium", "Low"};
            String[] statuses = new String[]{"To-do", "In Progress", "Done", "Archive"};
            for (int i = 1; i <= 30; i++) {
                LocalDateTime due = LocalDateTime.now().plusDays(i * 2L);
                String pri = priorities[i % priorities.length];
                TaskItem t = new TaskItem(UUID.randomUUID().toString(), "Task " + i + " — Assignment", due, pri);
                t.setSubject((i % 2 == 0) ? "Mathematics" : "History");
                t.setDescription("This is a sample description for Task " + i + ".\nRemember to review chapters and submit the assignment.");
                t.setStatus(statuses[i % statuses.length]);
                // mark some as completed for variety
                if (i % 5 == 0) t.setCompleted(true);
                tasks.put(t.getId(), t);
            }

            // sample reminders (structured)
            reminders.add(new Reminder(UUID.randomUUID().toString(), "Study session", "Custom", "16/11/2025", "18:00", "Review calculus notes"));
            reminders.add(new Reminder(UUID.randomUUID().toString(), "Project meeting", "Work", "17/11/2025", "09:00", "Weekly sync with team"));
            reminders.add(new Reminder(UUID.randomUUID().toString(), "Dentist appointment", "Personal", "19/11/2025", "14:30", "Annual checkup"));
            reminders.add(new Reminder(UUID.randomUUID().toString(), "Lab report due", "Urgent", "18/11/2025", "23:59", "Submit lab writeup"));

            saveToDisk();
            System.out.println("[MockDataService] no valid JSON found - seeded " + notes.size() + " notes and " + tasks.size() + " tasks");
        }
        else {
            System.out.println("[MockDataService] loaded existing data from JSON");
        }
    }

    public static MockDataService getInstance() {
        if (instance == null) instance = new MockDataService();
        return instance;
    }

    // Notes
    public Collection<Note> getAllNotes() { return notes.values(); }
    public Note getNote(String id) { return notes.get(id); }
    public void addNote(Note note) { notes.put(note.getId(), note); saveToDisk(); }
    public void removeNote(String id) { notes.remove(id); saveToDisk(); }

    // Tasks
    public Collection<TaskItem> getAllTasks() { return tasks.values(); }
    public TaskItem getTask(String id) { return tasks.get(id); }
    public void addTask(TaskItem task) { tasks.put(task.getId(), task); saveToDisk(); }
    public void removeTask(String id) { tasks.remove(id); saveToDisk(); }

    // Reminders (structured)
    public List<Reminder> getReminders() { return Collections.unmodifiableList(reminders); }
    public void addReminder(Reminder r) { reminders.add(r); saveToDisk(); }
    // legacy: accept string reminders and wrap them into a Reminder.message
    public void addReminder(String reminderString) { reminders.add(new Reminder(UUID.randomUUID().toString(), "", "Custom", "", "", reminderString)); saveToDisk(); }
    public void removeReminder(int index) { if (index >=0 && index < reminders.size()) { reminders.remove(index); saveToDisk(); } }
    // remove reminder by id
    public void removeReminder(String id) { if (id == null) return; Iterator<Reminder> it = reminders.iterator(); boolean removed = false; while (it.hasNext()) { Reminder r = it.next(); if (id.equals(r.getId())) { it.remove(); removed = true; break; } } if (removed) saveToDisk(); }

    // Simple JSON persistence (manual)
    private synchronized void saveToDisk() {
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            // notes
            sb.append("\"notes\":[");
            boolean first=true;
            for (Note n: notes.values()){
                if(!first) sb.append(",");
                first=false;
                sb.append('{');
                sb.append("\"id\":\"").append(escape(n.getId())).append("\",");
                sb.append("\"title\":\"").append(escape(n.getTitle())).append("\",");
                sb.append("\"content\":\"").append(escape(n.getContent())).append("\",");
                // tags array
                sb.append("\"tags\":[");
                boolean f2=true;
                for(String t: n.getTags()){ if(!f2) sb.append(','); f2=false; sb.append('"').append(escape(t)).append('"'); }
                sb.append("],");
                sb.append("\"created\":\"").append(n.getCreated().format(fmt)).append("\"");
                sb.append('}');
            }
            sb.append("],");
            // tasks
            sb.append("\"tasks\":[");
            first=true;
            for (TaskItem t: tasks.values()){
                if(!first) sb.append(','); first=false;
                sb.append('{');
                sb.append("\"id\":\"").append(escape(t.getId())).append("\",");
                sb.append("\"title\":\"").append(escape(t.getTitle())).append("\",");
                sb.append("\"due\":\"").append(t.getDueDate().format(fmt)).append("\",");
                sb.append("\"priority\":\"").append(escape(t.getPriority())).append("\",");
                sb.append("\"completed\":").append(t.isCompleted());
                // optional fields
                sb.append(",\"subject\":\"").append(escape(t.getSubject())).append("\"");
                sb.append(",\"description\":\"").append(escape(t.getDescription())).append("\"");
                sb.append(",\"status\":\"").append(escape(t.getStatus())).append("\"");
                sb.append('}');
            }
            sb.append("],");
            // reminders (structured)
            sb.append("\"reminders\":[");
            first=true;
            for(Reminder r: reminders){ if(!first) sb.append(','); first=false;
                sb.append('{');
                sb.append("\"id\":\"").append(escape(r.getId())).append("\",");
                sb.append("\"title\":\"").append(escape(r.getTitle())).append("\",");
                sb.append("\"type\":\"").append(escape(r.getType())).append("\",");
                sb.append("\"date\":\"").append(escape(r.getDate())).append("\",");
                sb.append("\"time\":\"").append(escape(r.getTime())).append("\",");
                sb.append("\"message\":\"").append(escape(r.getMessage())).append("\"");
                sb.append('}');
            }
            sb.append("]");

            sb.append("}");
            w.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadFromDisk(){
        if (!dataFile.exists()) return false;
        try {
            String s = new String(java.nio.file.Files.readAllBytes(dataFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            // very lightweight parsing - assumes well-formed produced by saveToDisk
            notes.clear(); tasks.clear(); reminders.clear();

            // helper: parse top-level JSON objects inside an array starting at index 'arrStart' (position of '[')
            // returns list of object strings including surrounding { }
            // implemented as a local lambda-style method via inner class (can't define lambdas easily here), so implement as private method below

            // parse notes array
            int ni = s.indexOf("\"notes\"");
            if (ni >= 0) {
                int arrStart = s.indexOf('[', ni);
                if (arrStart >= 0) {
                    List<String> objs = parseTopLevelObjects(s, arrStart);
                    for (String o : objs) {
                        String id = extractString(o, "id");
                        String title = extractString(o, "title");
                        String content = extractString(o, "content");
                        String created = extractString(o, "created");
                        Note n = new Note(id, title, content);
                        // tags
                        String tagsArr = extractArray(o, "tags");
                        if (tagsArr != null && !tagsArr.isBlank()){
                            String[] ts = tagsArr.split("\\s*,\\s*");
                            for(String t: ts){ t = t.trim(); if(t.startsWith("\"")&&t.endsWith("\"")) t = unescape(t.substring(1,t.length()-1)); n.getTags().add(t); }
                        }
                        notes.put(n.getId(), n);
                    }
                }
            }

            // parse tasks array
            int ti = s.indexOf("\"tasks\"");
            if (ti >= 0) {
                int arrStart = s.indexOf('[', ti);
                if (arrStart >= 0) {
                    List<String> objs = parseTopLevelObjects(s, arrStart);
                    for (String o : objs) {
                        String id = extractString(o, "id");
                        String title = extractString(o, "title");
                        String due = extractString(o, "due");
                        String priority = extractString(o, "priority");
                        String completed = extractLiteral(o, "completed");
                        TaskItem t = new TaskItem(id, title, null, priority);
                        t.setCompleted(completed != null && completed.equals("true"));
                        if (due != null) try { t.setDueDate(LocalDateTime.parse(due, fmt)); } catch (Exception e) {}
                        String subject = extractString(o, "subject");
                        if (subject != null) t.setSubject(unescape(subject));
                        String description = extractString(o, "description");
                        if (description != null) t.setDescription(unescape(description));
                        String status = extractString(o, "status");
                        if (status != null) t.setStatus(unescape(status));
                        tasks.put(t.getId(), t);
                    }
                }
            }

            // parse reminders array (support both string and structured objects)
            int ri = s.indexOf("\"reminders\"");
            if (ri >= 0) {
                int arrStart = s.indexOf('[', ri);
                if (arrStart >= 0) {
                    List<String> objs = parseTopLevelObjects(s, arrStart);
                    for (String p : objs) {
                        String v = p.trim();
                        if (v.startsWith("\"") && v.endsWith("\"")) {
                            v = unescape(v.substring(1, v.length()-1));
                            reminders.add(new Reminder(UUID.randomUUID().toString(), "", "Custom", "", "", v));
                        } else if (v.startsWith("{")) {
                            String id = extractString(v, "id");
                            String title = extractString(v, "title");
                            String type = extractString(v, "type");
                            String date = extractString(v, "date");
                            String time = extractString(v, "time");
                            String message = extractString(v, "message");
                            reminders.add(new Reminder(id != null ? id : UUID.randomUUID().toString(), title, type, date, time, message));
                        }
                    }
                }
            }

            // debug: print counts of loaded data so we can verify JSON parsing at runtime
            System.out.println("[MockDataService] loaded from JSON: notes=" + notes.size() + ", tasks=" + tasks.size() + ", reminders=" + reminders.size());
            return true;
        } catch (Exception e){ e.printStackTrace(); return false; }
    }

    // parses top-level objects or values inside an array starting at '[' position 'arrStart'
    private List<String> parseTopLevelObjects(String s, int arrStart){
        List<String> res = new ArrayList<>();
        int i = arrStart + 1;
        int len = s.length();
        boolean inQuotes = false;
        while (i < len) {
            // skip whitespace and commas
            char c = s.charAt(i);
            if (!inQuotes && (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == ',')) { i++; continue; }
            if (!inQuotes && c == ']') break; // end of array
            if (c == '"') { // a string value
                int start = i;
                i++; inQuotes = true;
                while (i < len) {
                    char d = s.charAt(i);
                    if (d == '\\') { i += 2; continue; } // skip escaped
                    if (d == '"') { inQuotes = false; i++; break; }
                    i++;
                }
                res.add(s.substring(start, i));
                continue;
            }
            if (c == '{') {
                int start = i;
                int depth = 1;
                i++;
                while (i < len && depth > 0) {
                    char d = s.charAt(i);
                    if (d == '"') {
                        // enter quoted string, skip until unescaped quote
                        i++;
                        while (i < len) {
                          char q = s.charAt(i);
                          if (q == '\\') { i += 2; continue; }
                          if (q == '"') { i++; break; }
                          i++;
                        }
                        continue;
                    }
                    if (d == '{') depth++;
                    else if (d == '}') depth--;
                    i++;
                }
                if (depth == 0) {
                    res.add(s.substring(start, i));
                } else break; // malformed
                continue;
            }
            // otherwise skip token until comma or closing bracket
            int start = i;
            while (i < len && s.charAt(i) != ',' && s.charAt(i) != ']') i++;
            res.add(s.substring(start, i).trim());
        }
        return res;
    }

    // helpers
    private String escape(String s){ if (s==null) return ""; return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }
    private String unescape(String s){ if (s==null) return ""; return s.replace("\\n","\n").replace("\\\"","\"").replace("\\\\","\\"); }
    private String extractString(String src, String key){
        int i = src.indexOf('"'+key+'"');
        if (i<0) return null;
        int col = src.indexOf(':', i);
        if (col<0) return null;
        int q1 = src.indexOf('"', col);
        if (q1<0) return null;
        int q2 = src.indexOf('"', q1+1);
        if (q2<0) return null;
        return unescape(src.substring(q1+1,q2));
    }
    private String extractLiteral(String src, String key){
        int i = src.indexOf('"'+key+'"'); if (i<0) return null; int col = src.indexOf(':', i); if(col<0) return null; int st = col+1; int enComma = src.indexOf(',',st); int enBrace = src.indexOf('}',st); int en = -1; if (enComma<0) en = enBrace; else if (enBrace<0) en = enComma; else en = Math.min(enComma,enBrace); if (en<0) en = src.length(); return src.substring(st,en).trim();
    }
    private String extractArray(String src, String key){
        int i = src.indexOf('"'+key+'"'); if (i<0) return null; int col = src.indexOf(':', i); if(col<0) return null; int b = src.indexOf('[',col); int e = src.indexOf(']',b); if(b<0||e<0) return null; return src.substring(b+1,e).trim();
    }

}
