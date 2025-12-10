package database;
import java.util.ArrayList;
import java.util.List;
import entity.user.User;
import entity.workitem.WorkItem;
import entity.sprint.Sprint;
public class AppDataBase {
    // --- Static Collections (The Database "Tables") ---
    private static final List<User> USERS = new ArrayList<>();
    private static final List<WorkItem> WORK_ITEMS = new ArrayList<>();
    private static final List<Sprint> SPRINTS = new ArrayList<>();
    // Static counters to generate unique IDs
    private static int nextUserId = 1;
    private static int nextWorkItemId = 1;
    private static int nextSprintId = 1;
    // --- Accessor Methods (Getters) ---
    public static List<User> getUsers() {
        return USERS;
    }
    public static List<WorkItem> getWorkItems() {
        return WORK_ITEMS;
    }
    public static List<Sprint> getSprints() {
        return SPRINTS;
    }

    // --- ID Generator Methods ---
    public static int getNextUserId() {
        return nextUserId++;
    }
    public static int getNextWorkItemId() {
        return nextWorkItemId++;
    }
    public static int getNextSprintId() {
        return nextSprintId++;
    }
    // Private constructor to prevent external instantiation.
    private AppDataBase() { }
}