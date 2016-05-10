package de.rwthaachen.openlap.analyticsengine.dataaccess;

import de.rwthaachen.openlap.analyticsengine.model.Indicator;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository to handle the Data Access Layer of the Indicators.
 * It is based on the spring CrudRepository.
 */
@Repository
public interface IndicatorRepository extends CrudRepository<Indicator, Long> {
}
