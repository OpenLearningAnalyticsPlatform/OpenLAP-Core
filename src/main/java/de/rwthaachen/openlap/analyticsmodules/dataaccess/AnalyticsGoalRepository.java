package de.rwthaachen.openlap.analyticsmodules.dataaccess;

import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by lechip on 27/11/15.
 */
public interface AnalyticsGoalRepository extends CrudRepository<AnalyticsGoal, String> {
}
