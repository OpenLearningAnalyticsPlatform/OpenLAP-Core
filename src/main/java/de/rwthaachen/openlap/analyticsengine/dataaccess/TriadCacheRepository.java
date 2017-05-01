package de.rwthaachen.openlap.analyticsengine.dataaccess;

import de.rwthaachen.openlap.analyticsengine.model.TriadCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Arham Muslim
 * on 03-Nov-16.
 */
@Repository
public interface TriadCacheRepository extends JpaRepository<TriadCache, Long> {
    List<TriadCache> findByTriadId(long triadId);
}
