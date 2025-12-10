package dao;
import entity.sprint.Sprint;
public interface ISprintDAO extends IDAO<Sprint> {
    Sprint findActiveSprint();
}