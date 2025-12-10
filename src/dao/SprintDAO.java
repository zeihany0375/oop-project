package dao;
import database.AppDataBase;
import entity.sprint.Sprint;
import java.util.List;
public class SprintDAO implements ISprintDAO {
    private final List<Sprint> sprints = AppDataBase.getSprints();
    @Override
    public Sprint create(Sprint entity) {
        sprints.add(entity);
        return entity;
    }
    @Override
    public Sprint findById(int id) {
        return sprints.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }
    @Override
    public List<Sprint> findAll() {
        return sprints;
    }
    @Override
    public Sprint findActiveSprint() {
        return sprints.isEmpty() ? null : sprints.get(sprints.size() - 1);
    }
    @Override
    public void update(Sprint entity) {
        if (findById(entity.getId()) == null) {
            System.err.println("Sprint not found for update: " + entity.getId());
        }
    }
    @Override
    public void delete(int id) {
        sprints.removeIf(s -> s.getId() == id);
    }
}
