package service;
import dao.ISprintDAO;
import database.AppDataBase;
import entity.sprint.Sprint;
import entity.workitem.WorkItem;
import entity.workitem.Task;
import entity.user.User;
import entity.user.TechnicalStaff;
import entity.user.ScrumMaster;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
public class SprintService {
    private final ISprintDAO sprintDAO;
    private final UserService userService; // Collaboration dependency
    private final TaskService taskService; // Collaboration dependency
    private static final int MAX_TEAM_CAPACITY = 40;
    public SprintService(ISprintDAO sprintDAO, UserService userService, TaskService taskService) {
        this.sprintDAO = sprintDAO;
        this.userService = userService;
        this.taskService = taskService;
    }
    public Sprint createSprint(String objective, LocalDate startDate, LocalDate endDate, User creator) {
        // --- STRICT BUSINESS RULE: Role Validation ---
        if (!(creator instanceof ScrumMaster)) {
            throw new BusinessRuleException("Sprint creation failed: Only the Scrum Master is authorized to create sprints.");
        }
        // Rule: Ensure end date is after start date
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("Sprint creation failed: End date must be after start date.");
        }

        int newId = AppDataBase.getNextSprintId();
        Sprint newSprint = new Sprint(newId, objective, startDate, endDate);

        return sprintDAO.create(newSprint);
    }
    public void commitToSprint(int sprintId, List<WorkItem> itemsToCommit, List<TechnicalStaff> team) {
        Sprint sprint = sprintDAO.findById(sprintId);
        if (sprint == null) {
            throw new BusinessRuleException("Sprint not found.");
        }
        // Rule: Calculate cumulative team capacity
        int totalTeamCapacity = team.size() * MAX_TEAM_CAPACITY;
        int committedHours = calculateCommittedHours(sprint);

        int newHours = itemsToCommit.stream()
                .filter(item -> item instanceof Task)
                .mapToInt(item -> ((Task) item).getEstimatedHours())
                .sum();

        // Strict Rule: Prevent overcommitment
        if (committedHours + newHours > totalTeamCapacity) {
            throw new BusinessRuleException("Commitment failed: Exceeds team capacity of " + totalTeamCapacity + " hours.");
        }
        // Add all items and members
        itemsToCommit.forEach(sprint::addWorkItem);
        team.forEach(sprint::addTeamMember);
        sprintDAO.update(sprint);

        System.out.println("Committed " + itemsToCommit.size() + " items to Sprint " + sprintId);
    }
    public Sprint getSprintById(int sprintId) {
        Sprint sprint = sprintDAO.findById(sprintId);
        if (sprint == null) {
            throw new BusinessRuleException("Sprint ID " + sprintId + " not found.");
        }
        return sprint;
    }
    public List<Task> getTasksForSprint(int sprintId) {
        Sprint sprint = sprintDAO.findById(sprintId);

        if (sprint == null) {
            throw new BusinessRuleException("Sprint ID " + sprintId + " not found.");
        }
        return sprint.getCommittedWork().stream()
                .filter(item -> item instanceof Task)
                .map(item -> (Task) item)
                .collect(Collectors.toList());
    }
    private int calculateCommittedHours(Sprint sprint) {
        return sprint.getCommittedWork().stream()
                .filter(item -> item instanceof Task)
                .mapToInt(item -> {
                    return ((Task) item).getEstimatedHours();
                })
                .sum();
    }
}