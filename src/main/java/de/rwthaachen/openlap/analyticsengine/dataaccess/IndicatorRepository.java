package de.rwthaachen.openlap.analyticsengine.dataaccess;

import de.rwthaachen.openlap.analyticsengine.model.Indicator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository to handle the Data Access Layer of the Indicators.
 * It is based on the spring CrudRepository.
 */
@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long> {
    List<Indicator> findByName(String name);
    List<Indicator> findByName(String name, Sort sort);
    List<Indicator> findByNameContaining(String name);
    List<Indicator> findByNameContaining(String name, Sort sort);
}
