package com.askinfra.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents one snapshot of performance metrics for a server at a point in time.
 *
 * Each row = one hourly reading for one server.
 * The DataSeeder creates 24 rows per server (one per hour over the last 24 hours).
 *
 * In Milestone 4 (Text-to-SQL), Gemini will receive the schema of this table
 * and generate SQL like:
 *   SELECT s.name, AVG(m.cpu_percent) FROM metrics m JOIN servers s ON ...
 */
@Entity
@Table(name = "metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Foreign key to the servers table */
    @Column(name = "server_id")
    private Long serverId;

    /** When this reading was captured */
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    /** CPU utilisation 0–100 */
    @Column(name = "cpu_percent")
    private Double cpuPercent;

    /** RAM utilisation 0–100 */
    @Column(name = "memory_percent")
    private Double memoryPercent;

    /** Disk utilisation 0–100 */
    @Column(name = "disk_percent")
    private Double diskPercent;
}
