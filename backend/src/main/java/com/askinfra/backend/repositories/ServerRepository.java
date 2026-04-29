package com.askinfra.backend.repositories;

import com.askinfra.backend.models.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for Server entities.
 *
 * By extending JpaRepository<Server, Long>, Spring auto-generates:
 *   save(), findById(), findAll(), deleteById(), count() ... and more.
 * You write zero SQL for standard CRUD operations.
 */
@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
}
