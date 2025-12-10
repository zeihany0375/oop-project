package entity.user;
import java.util.Objects;
public abstract class User {
    private int id;
    private String username;
    private String password;
    private String role; // Stores the concrete role name (e.g., "Developer")
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    // --- Encapsulation: Getters ---
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    // Polymorphic method
    public abstract String displayDashboard();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}