package dao;
import entity.user.User;
import java.util.List;
public interface IUserDAO extends IDAO<User> {
    User findByUsername(String username);
    User validateLogin(String username, String password);
    List<User> findUsersByRole(String role);
}