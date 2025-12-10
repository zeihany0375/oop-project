package entity.workitem;
import entity.user.User;
import java.util.ArrayList;
import java.util.List;
public class Story extends WorkItem {
    private List<Task> containedTasks;
    private int parentEpicId;

    public Story(int id, String title, String description, User creator, int parentEpicId) {
        super(id, title, description, creator);
        this.containedTasks = new ArrayList<>();
        this.parentEpicId = parentEpicId;
    }
    // --- Composition Methods ---
    public List<Task> getContainedTasks() { return containedTasks; }
    public int getParentEpicId() { return parentEpicId; }

    public void addTask(Task task) {
        this.containedTasks.add(task);
        task.setParentStoryId(this.getId());
    }

    @Override
    public String getWorkItemType() {
        return "Story";
    }
}
