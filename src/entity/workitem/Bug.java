package entity.workitem;
import entity.user.User;
public class Bug extends WorkItem {
    private String severity;
    private String stepsToReproduce;
    public Bug(int id, String title, String description, User creator, String severity) {
        super(id, title, description, creator);
        this.severity = severity;
        this.stepsToReproduce = "";
    }
    // --- Getters/Setters ---
    public String getSeverity() { return severity; }
    public String getStepsToReproduce() { return stepsToReproduce; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setStepsToReproduce(String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }
    @Override
    public String getWorkItemType() {
        return "Bug";
    }
}