package service;
import dao.IWorkItemDAO;
import database.AppDataBase;
import entity.workitem.*;
import entity.user.User;
import entity.user.Stakeholder;
import entity.user.QAengineer;
import java.util.List;
import java.util.stream.Collectors;
public class WorkItemService {
    private final IWorkItemDAO workItemDAO;
    public WorkItemService(IWorkItemDAO workItemDAO) {
        this.workItemDAO = workItemDAO;
    }
    public Epic createEpic(String title, String description, User creator) {
        if (!(creator instanceof Stakeholder)) {
            // throw new BusinessRuleException("Only Stakeholders can create high-level Epics.");
        }
        int newId = AppDataBase.getNextWorkItemId();
        // Pass the creator ID to the Epic constructor for strict accountability
        Epic newEpic = new Epic(newId, title, description, creator, creator.getId());

        return (Epic) workItemDAO.create(newEpic);
    }
    public Bug createBug(String title, String description, String severity, QAengineer reporter) {
        if (severity == null || severity.isEmpty()) {
            throw new BusinessRuleException("Bug must have a defined severity.");
        }

        int newId = AppDataBase.getNextWorkItemId();
        Bug newBug = new Bug(newId, title, description, reporter, severity);

        return (Bug) workItemDAO.create(newBug);
    }
    public Story createStory(String title, String description, User creator, int parentEpicId) {
        Epic parentEpic = (Epic) workItemDAO.findById(parentEpicId);
        if (parentEpic == null) {
            throw new BusinessRuleException("Cannot create Story: Parent Epic not found.");
        }
        int newId = AppDataBase.getNextWorkItemId();
        Story newStory = new Story(newId, title, description, creator, parentEpicId);
        // Composition Rule: Add the new Story to the Epic's internal list
        parentEpic.addStory(newStory);
        workItemDAO.update(parentEpic); // Persist the parent update

        return (Story) workItemDAO.create(newStory);
    }
    public WorkItem getWorkItemById(int id) {
        return workItemDAO.findById(id);
    }
    public List<WorkItem> getWorkItemsByCreator(int userId) {
        return workItemDAO.findAll().stream()
                .filter(item -> item.getCreator() != null && item.getCreator().getId() == userId)
                .collect(Collectors.toList());
    }
    public List<Epic> getAllEpics() {
        return workItemDAO.findAllEpics();
    }
}
