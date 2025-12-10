package service;
import dao.IUserDAO;
import database.AppDataBase;
import entity.user.*;
import java.util.List;
public class UserService {
    private final IUserDAO userDAO;
    public UserService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }
    public User registerUser(String username, String password, String role) {
        if (userDAO.findByUsername(username) != null) {
            throw new BusinessRuleException("Registration failed: Username already exists.");
        }
        int newId = AppDataBase.getNextUserId();
        User newUser = null;
        if ("Developer".equalsIgnoreCase(role)) {
            newUser = new Developer(newId, username, password);
        } else if ("QAEngineer".equalsIgnoreCase(role)) {
            newUser = new QAengineer(newId, username, password);
        } else if ("ScrumMaster".equalsIgnoreCase(role)) {
            newUser = new ScrumMaster(newId, username, password);
        } else if ("Stakeholder".equalsIgnoreCase(role)) {
            newUser = new Stakeholder(newId, username, password);
        } else {
            throw new BusinessRuleException("Invalid user role specified.");
        }
        return userDAO.create(newUser);
    }
    public User login(String username, String password) {
        User user = userDAO.validateLogin(username, password);
        if (user == null) {
            throw new BusinessRuleException("Login failed: Invalid username or password.");
        }
        System.out.println(user.getRole() + " " + user.getId() + " logged in successfully.");
        return user;
    }
    public User getUserById(int id) {
        return userDAO.findById(id);
    }
    public List<User> getUsersByRole(String role) {
        return userDAO.findUsersByRole(role);
    }
}