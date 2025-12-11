package main;
import entity.user.*;
import entity.workitem.*;
import entity.sprint.Sprint;
import service.*;
import dao.*;
import java.time.LocalDate;
import java.util.List;
public class Main {
    // Global DAO and Service instances (initialized in main)
    private static IUserDAO userDAO;
    private static IWorkItemDAO workItemDAO;
    private static ISprintDAO sprintDAO;
    private static UserService userService;
    private static TaskService taskService;
    private static SprintService sprintService;
    private static WorkItemService workItemService;
    // --- Initialized Users for testing ---
    private static Developer devA;
    private static Developer devB;
    private static QAengineer qaA;
    private static ScrumMaster smA;
    private static Stakeholder shA;
    public static void main(String[] args) {
        System.out.println("--- Enterprise Agile Task Coordinator Backend Test Suite ---");

        // --- 1. ARCHITECTURE SETUP (Dependency Injection) ---
        setupArchitecture();

        // --- 2. INITIAL DATA SETUP ---
        setupUsers();

        // --- 3. EXECUTE ALL TEST SUITES ---
        testSuiteUserManagement();
        testSuiteWorkItemCreationAndHierarchy();
        testSuiteTaskAssignmentAndWorkload();
        testSuiteBugWorkflow();
        testSuiteSprintPlanningAndRules();
        testSuiteDataRetrieval();
        testSuiteStoryValidation();

        System.out.println("\n--- ALL TEST SUITES COMPLETE ---");
    }

    private static void setupArchitecture() {
        // Initialize Concrete DAOs
        userDAO = new UserDAO();
        workItemDAO = new WorkItemDAO();
        sprintDAO = new SprintDAO();

        // Initialize Services via Dependency Injection
        userService = new UserService(userDAO);

        // TaskService now only requires two dependencies (workItemDAO, userDAO)
        taskService = new TaskService(workItemDAO, userDAO);

        // SprintService requires SprintDAO, UserService, and TaskService
        sprintService = new SprintService(sprintDAO, userService, taskService);

        // WorkItemService only requires WorkItemDAO
        workItemService = new WorkItemService(workItemDAO);
    }

    private static void setupUsers() {
        System.out.println("\n--- User Setup ---");
        devA = (Developer) userService.registerUser("ali_dev", "pass123", "Developer");
        devB = (Developer) userService.registerUser("sara_dev", "pass123", "Developer");
        qaA = (QAengineer) userService.registerUser("amir_qa", "pass123", "QAEngineer");
        smA = (ScrumMaster) userService.registerUser("zain_sm", "pass123", "ScrumMaster");
        shA = (Stakeholder) userService.registerUser("boss", "pass123", "Stakeholder");

        System.out.println("Status: 5 users created successfully.");
    }

    /**
     * Tests UserService methods: register, login, findByUsername, duplicate register.
     */
    private static void testSuiteUserManagement() {
        System.out.println("\n--- TEST SUITE 1: USER MANAGEMENT (UserService) ---");

        // Test: Successful Login
        User loggedIn = userService.login(devA.getUsername(), devA.getPassword());
        System.out.println("Test 1.1 (Login Success): Passed. Logged in as " + loggedIn.getRole());

        // Test: Failed Login (Wrong password)
        try {
            userService.login(devA.getUsername(), "wrong pass");
            System.err.println("Test 1.2 (Login Failure): FAILED. Expected exception not thrown.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 1.2 (Login Failure): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test: Duplicate Registration Failure (Strict Rule)
        try {
            userService.registerUser("ali_dev", "new pass", "Developer");
            System.err.println("Test 1.3 (Duplicate Register): FAILED. Expected exception not thrown.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 1.3 (Duplicate Register): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test: Find user by role (DAO Coverage)
        List<User> developers = userService.getUsersByRole("Developer");
        System.out.println("Test 1.4 (FindByRole): Found " + developers.size() + " developers (Expected: 2).");
    }

    /**
     * Tests WorkItemService and TaskService creation methods (Epic, Story, Bug, Task) and composition.
     */
    private static void testSuiteWorkItemCreationAndHierarchy() {
        System.out.println("\n--- TEST SUITE 2: WORK ITEM CREATION & HIERARCHY ---");

        // Test: Epic Creation (WorkItemService)
        Epic epic1 = workItemService.createEpic("Core Infrastructure", "Set up networking and CDN.", shA);
        System.out.println("Test 2.1 (Create Epic): SUCCESS. ID: " + epic1.getId());

        // Test: Story Creation (Composition via WorkItemService)
        Story story1 = workItemService.createStory("Setup CI/CD Pipeline", "Automate deployment.", smA, epic1.getId());
        System.out.println("Test 2.2 (Create Story): SUCCESS. Parent Epic ID: " + story1.getParentEpicId());

        // Test: Standalone Task Creation (TaskService)
        Task taskStandalone = taskService.createTask("Review PR Guidelines", "Update documentation.", devA, 2);
        System.out.println("Test 2.3 (Create Task): SUCCESS. Estimated Hours: " + taskStandalone.getEstimatedHours());

        // Test: Bug Creation (WorkItemService)
        Bug bug1 = workItemService.createBug("Login Button Alignment", "CSS alignment error on mobile.", "Minor", qaA);
        System.out.println("Test 2.4 (Create Bug): SUCCESS. Severity: " + bug1.getSeverity());

        // Test: Composition verification (DAO/Entity Coverage)
        Task taskComp = taskService.createTask("Write Dockerfile", "Containerize the service.", devB, 5);
        story1.addTask(taskComp);
        workItemDAO.update(story1);
        System.out.println("Test 2.5 (Composition): SUCCESS. Story 1 contains " + story1.getContainedTasks().size() + " tasks.");
    }

    /**
     * Tests TaskService assignment and capacity rules.
     */
    private static void testSuiteTaskAssignmentAndWorkload() {
        System.out.println("\n--- TEST SUITE 3: WORKLOAD & ASSIGNMENT (TaskService) ---");

        // Test: Successful Assignment (Task)
        Task smallTask = taskService.createTask("Debug Logging", "Fix debug log spam.", smA, 5);
        taskService.assignTaskToDeveloper(smallTask.getId(), devA.getId());
        int workloadAfterAssign = devA.getCurrentWorkloadHours();
        System.out.println("Test 3.1 (Assign Success): SUCCESS. Workload is now: " + workloadAfterAssign + "h (Expected: 5h).");

        // Test: Get assigned tasks (TaskService method coverage)
        List<Task> aliTasks = taskService.getTasksForUser(devA.getId());
        System.out.println("Test 3.2 (Get Tasks): SUCCESS. DevA has " + aliTasks.size() + " tasks assigned (Expected: 1).");

        // Test: Capacity Check Failure (Strict Rule: Max is 40h)
        Task overflowTask = taskService.createTask("Giant Refactor", "Max out capacity.", smA, 40); // 40h task
        try {
            taskService.assignTaskToDeveloper(overflowTask.getId(), devA.getId()); // 5h current + 40h new = 45h > 40h max
            System.err.println("Test 3.3 (Capacity Fail): FAILED. Expected capacity exception not thrown.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 3.3 (Capacity Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Clean up workload
        taskService.changeStatus(smallTask.getId(), "Done", devA); // Reduces workload by 5 (default for generic task)
        System.out.println("Status: Workload reset for next test.");
    }

    /**
     * Tests TaskService changeStatus and workflow rules (Task and Bug).
     */
    private static void testSuiteBugWorkflow() {
        System.out.println("\n--- TEST SUITE 4: WORKFLOW & BUG LOGIC (TaskService) ---");

        // A. TASK WORKFLOW VALIDATION
        Task linearTask = taskService.createTask("Validate Input Fields", "Check for SQL injection.", devA, 5);
        taskService.assignTaskToDeveloper(linearTask.getId(), devA.getId());

        // Test: Workflow Skip Failure (To Do -> Done)
        try {
            taskService.changeStatus(linearTask.getId(), "Done", devA);
            System.err.println("Test 4.1 (Task Skip Fail): FAILED. Expected skip exception not thrown.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 4.1 (Task Skip Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // B. BUG WORKFLOW VALIDATION (QA Permission Strict Rule)
        Bug criticalBug = workItemService.createBug("Memory Leak", "High priority leak.", "Critical", qaA);
        taskService.assignTaskToDeveloper(criticalBug.getId(), devA.getId()); // Workload increases by 8

        // Test: Dev moves Bug to Done (Should result in Ready for QA)
        taskService.changeStatus(criticalBug.getId(), "Done", devA);
        System.out.println("Test 4.2 (Dev Finish Bug): SUCCESS. Status moved to " + criticalBug.getStatus() + ".");

        // Test: Dev tries to close Bug (Strict Rule Failure)
        try {
            taskService.changeStatus(criticalBug.getId(), "Done", devA);
            System.err.println("Test 4.3 (Dev Closes Bug Fail): FAILED. Expected QA permission exception not thrown.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 4.3 (Dev Closes Bug Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test: Scrum Master Overrides Status (Process Facilitator)
        taskService.changeStatus(criticalBug.getId(), "Reopened", smA); // SM overrides and reopens
        System.out.println("Test 4.4 (SM Override): SUCCESS. Status is now " + criticalBug.getStatus() + ".");

        // Test: QA Closes Bug (Success)
        taskService.changeStatus(criticalBug.getId(), "Done", qaA); // QA closes the bug, workload decreases
        System.out.println("Test 4.5 (QA Closes Bug): SUCCESS. Status is now " + criticalBug.getStatus() + ".");
    }

    /**
     * Tests SprintService rules: role, date, and commitment capacity.
     */
    private static void testSuiteSprintPlanningAndRules() {
        System.out.println("\n--- TEST SUITE 5: SPRINT PLANNING (SprintService) ---");

        // Test: Role Validation Failure (DevA tries to create Sprint)
        LocalDate today = LocalDate.now();
        try {
            sprintService.createSprint("Unauthorized Sprint", today, today.plusDays(7), devA);
            System.err.println("Test 5.1 (Role Fail): FAILED. Expected Scrum Master role check exception.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 5.1 (Role Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test: Date Validation Failure (End date before start date)
        try {
            sprintService.createSprint("Bad Dates", today.plusDays(10), today.plusDays(5), smA);
            System.err.println("Test 5.2 (Date Fail): FAILED. Expected date validation exception.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 5.2 (Date Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test: Successful Sprint Creation
        Sprint sprintB = sprintService.createSprint("Release Sprint", today, today.plusDays(14), smA);
        System.out.println("Test 5.3 (Creation Success): Created Sprint ID: " + sprintB.getId());

        // Test: Overcommitment Failure (Strict Rule: Team capacity is 3 * 40h = 120h)
        Task hugeTask1 = taskService.createTask("Huge 65h Task", "Break this rule.", smA, 65);
        Task hugeTask2 = taskService.createTask("Huge 65h Task 2", "Break this rule.", smA, 65);
        List<TechnicalStaff> smallTeam = List.of(devA, devB, qaA); // 120h capacity
        List<WorkItem> overcommittedWork = List.of(hugeTask1, hugeTask2); // Total 130h

        try {
            sprintService.commitToSprint(sprintB.getId(), overcommittedWork, smallTeam);
            System.err.println("Test 5.4 (Overcommit Fail): FAILED. Expected capacity exception.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 5.4 (Overcommit Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test: Successful Commitment (Commit 20h tasks)
        Task safeTask10A = taskService.createTask("10h Task", "", smA, 10);
        Task safeTask10B = taskService.createTask("10h Task B", "", smA, 10);
        List<WorkItem> safeWork = List.of(safeTask10A, safeTask10B); // Total 20h
        sprintService.commitToSprint(sprintB.getId(), safeWork, smallTeam);
        System.out.println("Test 5.5 (Commit Success): SUCCESS. 20h committed successfully.");
    }

    /**
     * Tests various data retrieval methods (DAO and Service coverage) and wrapper methods.
     */
    private static void testSuiteDataRetrieval() {
        System.out.println("\n--- TEST SUITE 6: DATA RETRIEVAL COVERAGE ---");

        // Test: Get all Epics (WorkItemService)
        List<Epic> allEpics = workItemService.getAllEpics();
        System.out.println("Test 6.1 (GetAllEpics): SUCCESS. Found " + allEpics.size() + " Epics (Expected >= 1).");

        // Test: Get items created by Stakeholder (WorkItemService)
        List<WorkItem> itemsBySH = workItemService.getWorkItemsByCreator(shA.getId());
        System.out.println("Test 6.2 (GetItemsByCreator): SUCCESS. Found " + itemsBySH.size() + " items created by SH (Expected >= 1).");

        // Test: Get Tasks for Sprint (SprintService)
        Sprint lastSprint = sprintDAO.findActiveSprint();
        if (lastSprint != null) {
            List<Task> sprintTasks = sprintService.getTasksForSprint(lastSprint.getId());
            System.out.println("Test 6.3 (GetTasksForSprint): SUCCESS. Found " + sprintTasks.size() + " tasks in last sprint (Expected >= 2).");
        }

        // Test: Get Sprint by ID (SprintService wrapper)
        try {
            Sprint foundSprint = sprintService.getSprintById(lastSprint.getId());
            System.out.println("Test 6.4 (GetSprintById): SUCCESS. Retrieved Sprint ID: " + foundSprint.getId());
        } catch (Exception e) {
            System.err.println("Test 6.4 (GetSprintById): FAILED to retrieve sprint.");
        }

        // Test: DAO findByStatus coverage (Manual DAO call)
        List<WorkItem> doneItems = workItemDAO.findItemsByStatus("Done");
        System.out.println("Test 6.5 (FindByStatus): Found " + doneItems.size() + " items marked 'Done' (Expected >= 2).");
    }

    /**
     * Tests TaskService validateStoryCompletion rule (Composition Integrity).
     */
    private static void testSuiteStoryValidation() {
        System.out.println("\n--- TEST SUITE 7: STORY COMPLETION VALIDATION ---");

        // 1. Setup: Create a new hierarchy
        Epic epic2 = workItemService.createEpic("Story Validation Epic", "Testing story completeness.", shA);
        Story story2 = workItemService.createStory("Feature with Subtasks", "Must complete all tasks before finishing.", smA, epic2.getId());

        Task taskX = taskService.createTask("Subtask X", "Requires 10h.", devA, 10);
        Task taskY = taskService.createTask("Subtask Y", "Requires 5h.", devA, 5);

        story2.addTask(taskX);
        story2.addTask(taskY);
        workItemDAO.update(story2);

        // Assign and start tasks
        taskService.assignTaskToDeveloper(taskX.getId(), devA.getId());
        taskService.assignTaskToDeveloper(taskY.getId(), devA.getId());
        taskService.changeStatus(taskX.getId(), "In Progress", devA);

        // Test 7.1 (Failure): Try to validate Story completion while Task X is In Progress.
        try {
            taskService.validateStoryCompletion(story2.getId());
            System.err.println("Test 7.1 (Story Fail): FAILED. Expected validation exception not thrown.");
        } catch (BusinessRuleException e) {
            System.out.println("Test 7.1 (Story Fail): SUCCESS. Caught expected exception: " + e.getMessage());
        }

        // Test 7.2 (Success): Complete all subtasks and validate.
        taskService.changeStatus(taskX.getId(), "Done", devA);
        taskService.changeStatus(taskY.getId(), "Done", devA);

        try {
            taskService.validateStoryCompletion(story2.getId());
            System.out.println("Test 7.2 (Story Success): SUCCESS. Story status is now " + story2.getStatus());
        } catch (BusinessRuleException e) {
            System.err.println("Test 7.2 (Story Success): FAILED. Unexpected exception thrown: " + e.getMessage());
        }
    }
}

