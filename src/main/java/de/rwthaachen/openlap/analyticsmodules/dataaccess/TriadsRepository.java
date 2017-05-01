package de.rwthaachen.openlap.analyticsmodules.dataaccess;

import de.rwthaachen.openlap.analyticsmodules.model.IndicatorReference;
import de.rwthaachen.openlap.analyticsmodules.model.Triad;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository to handle the Data Access Layer of the Triads.
 * It is based on the spring CrudRepository.
 */
@Repository
public interface TriadsRepository extends JpaRepository<Triad, Long> {
    List<Triad> findByCreatedBy(String createdBy);
}
