package dao;
import database.AppDataBase;
import entity.user.User;
import java.util.List;
public class UserDAO implements IUserDAO {
    private final List<User> users = AppDataBase.getUsers();
    @Override
    public User create(User entity) {
        users.add(entity);
        return entity;
    }
    @Override
    public User findById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }
    @Override
    public User findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }
    @Override
    public User validateLogin(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }
    @Override
    public List<User> findAll() {
        return users;
    }
    @Override
    public void update(User entity) {
        if (findById(entity.getId()) == null) {
            System.err.println("User not found for update: " + entity.getId());
        }
    }
    @Override
    public void delete(int id) {
        users.removeIf(u -> u.getId() == id);
    }
    @Override
    public List<User> findUsersByRole(String role) {
        return users.stream().filter(u -> u.getRole().equalsIgnoreCase(role)).toList();
    }
}