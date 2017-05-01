package de.rwthaachen.openlap.analyticsengine.dataaccess;

import de.rwthaachen.openlap.analyticsengine.model.Question;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository to handle the Data Access Layer of the Questions.
 * It is based on the spring CrudRepository.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByName(String name);
    List<Question> findByName(String name, Sort sort);
    List<Question> findByNameContaining(String name);
    List<Question> findByNameContaining(String name, Sort sort);
}


