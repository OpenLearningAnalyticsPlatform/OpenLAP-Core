package de.rwthaachen.openlap.analyticsmethods.dataaccess;

import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository to handle the Data Access Layer of the AnalyticsMethods.
 * It is based on the spring CrudRepository.
 */
@Repository
public interface AnalyticsMethodsRepository
        extends CrudRepository<AnalyticsMethodMetadata, Long> {
    List<AnalyticsMethodMetadata> findAllByOrderByNameAsc();
}
