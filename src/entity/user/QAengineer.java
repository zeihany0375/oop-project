package entity.user;
public class QAengineer extends TechnicalStaff {
    public QAengineer(int id, String username, String password) {
        super(id, username, password, "QAEngineer");
    }
    @Override
    public String displayDashboard() {
        return "QA Engineer Dashboard: Bugs to Verify & Assigned Tasks";
    }
}