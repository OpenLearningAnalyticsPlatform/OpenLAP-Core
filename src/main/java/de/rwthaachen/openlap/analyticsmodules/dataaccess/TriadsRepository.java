package de.rwthaachen.openlap.analyticsmodules.dataaccess;

import de.rwthaachen.openlap.analyticsmodules.model.Triad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository to handle the Data Access Layer of the Triads.
 * It is based on the spring CrudRepository.
 */
@Repository
public interface TriadsRepository extends CrudRepository<Triad, String> {

}
