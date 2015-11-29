package org.rwthaachen.olap.analyticsmodules.dataAccess;

import org.rwthaachen.olap.analyticsmodules.model.Triad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lechip on 27/11/15.
 */
@Repository
public interface TriadsRepository extends CrudRepository<Triad, String> {

}
