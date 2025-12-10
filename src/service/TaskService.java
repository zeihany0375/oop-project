package service;

import dao.IWorkItemDAO;
import dao.IUserDAO;
import entity.workitem.*;
import entity.user.*;
import java.util.List;
import database.AppDataBase;
import java.util.stream.Collectors;
public class TaskService {
    private final IWorkItemDAO workItemDAO;
    private final UserService userService; // Collaboration with another service
    private final IUserDAO userDAO; // Direct DAO dependency
    // Constants for status strings (Template of the Task workflow)
    private static final String STATUS_TODO = "To Do";
    private static final String STATUS_IN_PROGRESS = "In Progress";
    private static final String STATUS_READY_FOR_QA = "Ready for QA";
    private static final String STATUS_DONE = "Done";
    private static final String STATUS_REOPENED = "Reopened";
    public TaskService(IWorkItemDAO workItemDAO, UserService userService, IUserDAO userDAO) {
        this.workItemDAO = workItemDAO;
        this.userService = userService;
        this.userDAO = userDAO;
    }
    public Task createTask(String title, String description, User creator, int estimatedHours) {
        if (estimatedHours <= 0) {
            throw new BusinessRuleException("Task must have a positive estimated hours value.");
        }

        int newId = AppDataBase.getNextWorkItemId();
        Task newTask = new Task(newId, title, description, creator, estimatedHours);

        return (Task) workItemDAO.create(newTask);
    }
    public List<Task> getTasksForUser(int userId) {
        List<WorkItem> assignedItems = workItemDAO.findItemsByAssignee(userId);

        return assignedItems.stream()
                .filter(item -> item instanceof Task)
                .map(item -> (Task) item)
                .collect(Collectors.toList());
    }
    public void assignTaskToDeveloper(int workItemId, int developerId) {
        WorkItem item = workItemDAO.findById(workItemId);
        Developer assignee = (Developer) userDAO.findById(developerId);

        if (item == null || assignee == null || !(assignee instanceof Developer)) {
            throw new BusinessRuleException("Assignment failed: Item or Developer not found.");
        }
        // --- Strict Business Rule: Capacity Check ---
        int itemHours = (item instanceof Task) ? ((Task) item).getEstimatedHours() : 8; // Assume 8 hrs for Bug

        if (assignee.getCurrentWorkloadHours() + itemHours > assignee.getMaxCapacityHours()) {
            throw new BusinessRuleException("Assignment failed: Developer capacity exceeded.");
        }
        item.setAssignee(assignee);
        item.setStatus(STATUS_IN_PROGRESS);
        assignee.addToWorkload(itemHours);

        workItemDAO.update(item);
        System.out.println("Assigned " + item.getWorkItemType() + " " + workItemId + " to Developer " + assignee.getId());
    }
    public void changeStatus(int workItemId, String newStatus, User currentUser) {
        WorkItem item = workItemDAO.findById(workItemId);

        if (item == null) { throw new BusinessRuleException("Work Item not found."); }
        // Rule: Only the assigned user or ScrumMaster can change status
        if (!currentUser.equals(item.getAssignee()) && !(currentUser instanceof ScrumMaster)) {
            throw new BusinessRuleException("User " + currentUser.getId() + " is not authorized to change status.");
        }
        if (item instanceof Bug) {
            handleBugWorkflow((Bug) item, newStatus, currentUser);
        } else {
            handleGenericWorkflow(item, newStatus);
        }
        workItemDAO.update(item);
        System.out.println(item.getWorkItemType() + " " + workItemId + " status changed to: " + newStatus);
    }
    private void handleBugWorkflow(Bug bug, String newStatus, User currentUser) {
        String currentStatus = bug.getStatus();
        if (STATUS_IN_PROGRESS.equals(currentStatus) && STATUS_DONE.equals(newStatus)) {
            bug.setStatus(STATUS_READY_FOR_QA);
        } else if (STATUS_READY_FOR_QA.equals(currentStatus) && STATUS_DONE.equals(newStatus)) {
            if (!(currentUser instanceof QAengineer)) {
                throw new BusinessRuleException("Only a QA Engineer can close a Bug after verification.");
            }
            bug.setStatus(STATUS_DONE);
            ((Developer)bug.getAssignee()).removeFromWorkload(8);
        } else if (STATUS_READY_FOR_QA.equals(currentStatus) && STATUS_REOPENED.equals(newStatus)) {
            if (!(currentUser instanceof QAengineer)) {
                throw new BusinessRuleException("Only a QA Engineer can reopen a Bug.");
            }
            bug.setStatus(STATUS_REOPENED);
        } else {
            bug.setStatus(newStatus);
        }
    }
    private void handleGenericWorkflow(WorkItem item, String newStatus) {
        if (STATUS_TODO.equals(item.getStatus()) && STATUS_DONE.equals(newStatus)) {
            throw new BusinessRuleException("Cannot skip statuses: To Do must move to In Progress first.");
        }
        item.setStatus(newStatus);

        if (STATUS_DONE.equals(newStatus) && item.getAssignee() instanceof Developer) {
            ((Developer)item.getAssignee()).removeFromWorkload(5);
        }
    }
    public void validateStoryCompletion(int storyId) {
        Story story = (Story) workItemDAO.findById(storyId);

        for (Task task : story.getContainedTasks()) {
            if (!STATUS_DONE.equals(task.getStatus())) {
                throw new BusinessRuleException("Story cannot be completed: Not all Tasks are done.");
            }
        }
        story.setStatus(STATUS_DONE);
        workItemDAO.update(story);
    }
}