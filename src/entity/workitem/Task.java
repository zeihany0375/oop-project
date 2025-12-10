package entity.workitem;
import entity.user.User;
public class Task extends WorkItem {
    private int estimatedHours;
    private Integer parentStoryId;

    public Task(int id, String title, String description, User creator, int estimatedHours) {
        super(id, title, description, creator);
        this.estimatedHours = estimatedHours;
    }
    // --- Getters/Setters ---
    public int getEstimatedHours() { return estimatedHours; }
    public Integer getParentStoryId() { return parentStoryId; }

    public void setEstimatedHours(int estimatedHours) { this.estimatedHours = estimatedHours; }
    public void setParentStoryId(Integer parentStoryId) { this.parentStoryId = parentStoryId; }

    @Override
    public String getWorkItemType() {
        return "Task";
    }
}