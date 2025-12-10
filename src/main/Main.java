package main;
import entity.user.*;
import entity.workitem.*;
import entity.sprint.Sprint;
import service.*;
import dao.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
public class Main {
    public static void main(String[] args) {
        System.out.println("--- Enterprise Agile Task Coordinator Backend Test ---");
        // --- 1. INITIALIZE DAO LAYER (Concrete Implementations) ---
        IUserDAO userDAO = new UserDAO();
        IWorkItemDAO workItemDAO = new WorkItemDAO();
        ISprintDAO sprintDAO = new SprintDAO();
        // --- 2. INITIALIZE SERVICE LAYER (DI: Injecting DAOs and Services) ---
        UserService userService = new UserService(userDAO);
        // TaskService requires WorkItemDAO, UserService, and IUserDAO
        TaskService taskService = new TaskService(workItemDAO, userService, userDAO);
        // SprintService requires SprintDAO, UserService, and TaskService
        SprintService sprintService = new SprintService(sprintDAO, userService, taskService);
        // WorkItemService only requires WorkItemDAO
        WorkItemService workItemService = new WorkItemService(workItemDAO);
        // --- 3. TEST USER REGISTRATION & LOGIN (UserService) ---
        System.out.println("\n--- 3. User Management ---");
        Developer dev1 = (Developer) userService.registerUser("ali_dev", "pass123", "Developer");
        Developer dev2 = (Developer) userService.registerUser("sara_dev", "pass123", "Developer");
        QAengineer qa1 = (QAengineer) userService.registerUser("amir_qa", "pass123", "QAEngineer");
        ScrumMaster sm1 = (ScrumMaster) userService.registerUser("zain_sm", "pass123", "ScrumMaster");
        Stakeholder sh1 = (Stakeholder) userService.registerUser("boss", "pass123", "Stakeholder");
        User loggedInUser = userService.login("ali_dev", "pass123");
        // --- 4. WORK ITEM CREATION (WorkItemService & TaskService) ---
        System.out.println("\n--- 4. Work Item Creation & Hierarchy ---");
        // Epic Creation (Stakeholder initiates high-level request)
        Epic epic1 = workItemService.createEpic("Implement New Search Feature", "Build a high-speed search index.", sh1);
        System.out.println("Created Epic: " + epic1.getId() + " - " + epic1.getTitle());
        // Story Creation (Composition/Hierarchy)
        Story story1 = workItemService.createStory("Frontend UI for Search", "Design the search bar and results page.", dev1, epic1.getId());
        // Standalone Task Creation (TaskService)
        Task task1 = taskService.createTask("Write SQL Query for Search", "Optimize the primary search query.", dev1, 8);
        Task task2 = taskService.createTask("Build React Component", "Code the search component in React.", dev1, 10);
        // Composition: Add tasks to Story's internal list
        story1.addTask(task1);
        story1.addTask(task2);
        workItemDAO.update(story1); // Save the updated Story object
        // Bug Creation (Standalone)
        Bug bug1 = workItemService.createBug("Database connection pooling issue.", "DB connections are leaking memory.", "Blocker", qa1);
        // --- 5. TASK/BUG ASSIGNMENT & WORKFLOW (TaskService) ---
        System.out.println("\n--- 5. Task/Bug Assignment & Workflow ---");
        // Assign Task 1 (Checks capacity)
        taskService.assignTaskToDeveloper(task1.getId(), dev1.getId());
        System.out.println("Dev1 Workload: " + dev1.getCurrentWorkloadHours() + " hours."); // Should be 8
        // Try to assign Bug (Checks capacity)
        taskService.assignTaskToDeveloper(bug1.getId(), dev1.getId());
        System.out.println("Dev1 Workload: " + dev1.getCurrentWorkloadHours() + " hours."); // Should be 8 + 8 = 16
        // Developer completes fix on Bug (Bug Logic: cannot set to final Done)
        taskService.changeStatus(bug1.getId(), "Done", dev1);
        System.out.println("Bug status after fix attempt: " + bug1.getStatus()); // Should be Ready for QA
        // QA verifies and closes Bug (Bug Logic: Only QA can close)
        taskService.changeStatus(bug1.getId(), "Done", qa1);
        System.out.println("Bug status after QA close: " + bug1.getStatus()); // Should be Done
        System.out.println("Dev1 Workload after Bug closure: " + dev1.getCurrentWorkloadHours() + " hours."); // Should be 16 - 8 = 8
        // --- 6. SPRINT PLANNING (SprintService) ---
        System.out.println("\n--- 6. Sprint Planning & Rules ---");
        // Create Sprint (Role & Date Validation)
        LocalDate today = LocalDate.now();
        Sprint sprint1 = sprintService.createSprint("Q4 Feature Rollout", today, today.plusDays(14), sm1);
        System.out.println("Created Sprint: " + sprint1.getObjective() + " (ID: " + sprint1.getId() + ")");
        // Commit work to Sprint (Checks capacity against team)
        List<TechnicalStaff> team = List.of(dev1, dev2, qa1);
        List<WorkItem> workToCommit = List.of(story1, task2);
        sprintService.commitToSprint(sprint1.getId(), workToCommit, team);
        // Display Sprint Data (New Method)
        List<Task> committedTasks = sprintService.getTasksForSprint(sprint1.getId());
        System.out.println("Tasks committed to Sprint 1: " + committedTasks.stream().map(Task::getTitle).collect(Collectors.joining(", ")));
        // Test Strict Rule: Fail to register duplicate user
        try {
            userService.registerUser("ali_dev", "newpass", "Developer");
        } catch (BusinessRuleException e) {
            System.err.println("TEST FAILURE: " + e.getMessage());
        }
    }
}