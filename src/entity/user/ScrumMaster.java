package entity.user;
public class ScrumMaster extends User {
    public ScrumMaster(int id, String username, String password) {
        super(id, username, password, "ScrumMaster");
    }

    @Override
    public String displayDashboard() {
        return "Scrum Master Dashboard: Sprint Metrics & Team Capacity";
    }
}