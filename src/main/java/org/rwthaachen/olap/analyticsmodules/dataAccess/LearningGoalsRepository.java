package org.rwthaachen.olap.analyticsmodules.dataAccess;

import org.rwthaachen.olap.analyticsmodules.model.LearningGoal;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by lechip on 27/11/15.
 */
public interface LearningGoalsRepository extends CrudRepository<LearningGoal, String> {
}
