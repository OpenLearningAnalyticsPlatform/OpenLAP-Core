package org.rwthaachen.olap.analyticsmethods.dataAccess;

import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lechip on 05/11/15.
 */
@Repository
public interface AnalyticsMethodsRepository
        extends CrudRepository<AnalyticsMethodMetadata, String>
{
}
