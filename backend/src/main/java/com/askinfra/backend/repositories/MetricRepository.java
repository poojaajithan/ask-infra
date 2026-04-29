package com.askinfra.backend.repositories;

import com.askinfra.backend.models.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for Metric entities.
 *
 * findByServerId() is a "derived query method" — Spring reads the method name
 * and automatically generates: SELECT * FROM metrics WHERE server_id = ?
 * No SQL or @Query annotation needed.
 */
@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findByServerId(Long serverId);
}
