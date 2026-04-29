package com.askinfra.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a monitored infrastructure server.
 *
 * This is a JPA @Entity — Spring will create a "servers" table in H2 for it.
 * Lombok annotations (@Data, @Builder, etc.) generate boilerplate at compile time:
 *   @Data           → getters, setters, equals, hashCode, toString
 *   @Builder        → fluent builder pattern: Server.builder().name("x").build()
 *   @NoArgsConstructor / @AllArgsConstructor → constructors JPA and tests need
 */
@Entity
@Table(name = "servers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable hostname, e.g. "web-server-1" */
    private String name;

    /** Server role: "web", "database", "cache", "api-gateway", "message-broker" */
    private String type;

    /** Deployment region, e.g. "us-east-1", "eu-west-1" */
    private String region;
}
