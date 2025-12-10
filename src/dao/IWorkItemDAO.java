package dao;
import entity.workitem.WorkItem;
import entity.workitem.Epic;
import entity.workitem.Story;
import entity.workitem.Task;
import java.util.List;
public interface IWorkItemDAO extends IDAO<WorkItem> {
    List<WorkItem> findItemsByStatus(String status);
    List<WorkItem> findItemsByAssignee(int userId);
    List<Epic> findAllEpics();
    List<Story> findStoriesByEpicId(int epicId);
    List<Task> findTasksByStoryId(int storyId);
}
