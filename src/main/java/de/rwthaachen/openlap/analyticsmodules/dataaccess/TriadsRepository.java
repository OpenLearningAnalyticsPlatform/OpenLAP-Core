package de.rwthaachen.openlap.analyticsmodules.dataaccess;

import de.rwthaachen.openlap.analyticsmodules.model.Triad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lechip on 27/11/15.
 */
@Repository
public interface TriadsRepository extends CrudRepository<Triad, String> {

}
