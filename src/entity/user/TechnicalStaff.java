package entity.user;

public abstract class TechnicalStaff extends User {
    private int maxCapacityHours = 40;
    private int currentWorkloadHours = 0;

    public TechnicalStaff(int id, String username, String password, String role) {
        super(id, username, password, role);
    }

    // --- Capacity Management ---
    public int getMaxCapacityHours() {
        return maxCapacityHours;
    }

    public int getCurrentWorkloadHours() {
        return currentWorkloadHours;
    }

    public void addToWorkload(int hours) {
        this.currentWorkloadHours += hours;
    }

    public void removeFromWorkload(int hours) {
        this.currentWorkloadHours = Math.max(0, this.currentWorkloadHours - hours);
    }
}