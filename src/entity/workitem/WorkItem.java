package entity.workitem;
import entity.user.User;
import java.time.LocalDateTime;
public abstract class WorkItem {
    private int id;
    private String title;
    private String description;
    private String status;
    private User assignee;
    private String priority;
    private User creator;
    private LocalDateTime dateCreated;
    private static final String DEFAULT_STATUS = "To Do";
    public WorkItem(int id, String title, String description, User creator) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.dateCreated = LocalDateTime.now();
        this.status = DEFAULT_STATUS;
        this.priority = "Medium";
    }
    // --- Encapsulation: Getters and Setters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public User getAssignee() { return assignee; }
    public String getPriority() { return priority; }
    public User getCreator() { return creator; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setAssignee(User assignee) { this.assignee = assignee; }
    public void setPriority(String priority) { this.priority = priority; }
    public abstract String getWorkItemType();
}