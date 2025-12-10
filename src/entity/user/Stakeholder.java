package entity.user;
public class Stakeholder extends User {
    public Stakeholder(int id, String username, String password) {
        super(id, username, password, "Stakeholder");
    }

    @Override
    public String displayDashboard() {
        return "Stakeholder Dashboard: Epic Progress & High-Level Requests";
    }
}