package entity.user;
public class Developer extends TechnicalStaff {
    public Developer(int id, String username, String password) {
        super(id, username, password, "Developer");
    }

    @Override
    public String displayDashboard() {
        return "Developer Dashboard: Assigned Tasks & Current Workload";
    }
}