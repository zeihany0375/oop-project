package entity.workitem;
import entity.user.User;
import java.util.ArrayList;
import java.util.List;
public class Epic extends WorkItem {
    private List<Story> containedStories;
    private int stakeholderId; // ID of the Stakeholder who owns the Epic

    public Epic(int id, String title, String description, User creator, int stakeholderId) {
        super(id, title, description, creator);
        this.containedStories = new ArrayList<>();
        this.stakeholderId = stakeholderId;
    }
    // --- Composition Methods ---
    public List<Story> getContainedStories() { return containedStories; }
    public int getStakeholderId() { return stakeholderId; }

    public void addStory(Story story) {
        this.containedStories.add(story);
    }
    @Override
    public String getWorkItemType() {
        return "Epic";
    }
}