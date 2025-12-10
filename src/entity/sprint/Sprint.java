package entity.sprint;
import entity.workitem.WorkItem;
import entity.user.TechnicalStaff;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class Sprint {
    private int id;
    private String objective;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<WorkItem> committedWork;
    private List<TechnicalStaff> assignedTeam;

    public Sprint(int id, String objective, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.objective = objective;
        this.startDate = startDate;
        this.endDate = endDate;
        this.committedWork = new ArrayList<>();
        this.assignedTeam = new ArrayList<>();
    }

    // --- Encapsulation: Getters and Setters ---
    public int getId() { return id; }
    public String getObjective() { return objective; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<WorkItem> getCommittedWork() { return committedWork; }
    public List<TechnicalStaff> getAssignedTeam() { return assignedTeam; }

    public void setObjective(String objective) { this.objective = objective; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public void addWorkItem(WorkItem item) {
        this.committedWork.add(item);
    }

    public void addTeamMember(TechnicalStaff member) {
        this.assignedTeam.add(member);
    }
}