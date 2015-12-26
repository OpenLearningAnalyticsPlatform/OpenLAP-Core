package de.rwthaachen.openlap.analyticsmodules.dataaccess;

import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import org.springframework.data.repository.CrudRepository;

/**
 * CRUD repository to handle the Data Access Layer of the AnalyticsGoals.
 * It is based on the spring CrudRepository.
 */
public interface AnalyticsGoalRepository extends CrudRepository<AnalyticsGoal, String> {
}
