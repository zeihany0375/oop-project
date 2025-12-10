package dao;
import database.AppDataBase;
import entity.workitem.*;
import java.util.List;
import java.util.stream.Collectors;
public class WorkItemDAO implements IWorkItemDAO {
    private final List<WorkItem> workItems = AppDataBase.getWorkItems();
    @Override
    public WorkItem create(WorkItem entity) {
        workItems.add(entity);
        return entity;
    }
    @Override
    public WorkItem findById(int id) {
        return workItems.stream().filter(w -> w.getId() == id).findFirst().orElse(null);
    }
    @Override
    public List<WorkItem> findAll() {
        return workItems;
    }
    @Override
    public List<WorkItem> findItemsByStatus(String status) {
        return workItems.stream()
                .filter(w -> w.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }
    @Override
    public List<WorkItem> findItemsByAssignee(int userId) {
        return workItems.stream()
                .filter(w -> w.getAssignee() != null && w.getAssignee().getId() == userId)
                .collect(Collectors.toList());
    }
    @Override
    public List<Epic> findAllEpics() {
        return workItems.stream()
                .filter(w -> w instanceof Epic)
                .map(w -> (Epic) w)
                .collect(Collectors.toList());
    }
    @Override
    public List<Story> findStoriesByEpicId(int epicId) {
        return workItems.stream()
                .filter(w -> w instanceof Story)
                .map(w -> (Story) w)
                .filter(s -> s.getParentEpicId() == epicId)
                .collect(Collectors.toList());
    }
    @Override
    public List<Task> findTasksByStoryId(int storyId) {
        return workItems.stream()
                .filter(w -> w instanceof Task)
                .map(w -> (Task) w)
                .filter(t -> t.getParentStoryId() != null && t.getParentStoryId() == storyId)
                .collect(Collectors.toList());
    }
    // --- CRUD for list management ---
    @Override
    public void update(WorkItem entity) {
        if (!workItems.contains(entity)) {
            System.err.println("WorkItem not found for update: " + entity.getId());
        }
    }
    @Override
    public void delete(int id) {
        workItems.removeIf(w -> w.getId() == id);
    }
}