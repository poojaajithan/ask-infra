package com.askinfra.backend.config;

import com.askinfra.backend.models.Metric;
import com.askinfra.backend.models.Server;
import com.askinfra.backend.repositories.MetricRepository;
import com.askinfra.backend.repositories.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds the H2 in-memory database with realistic infrastructure telemetry data.
 *
 * CommandLineRunner: Spring calls the run() method automatically after the
 * application context is fully started. Think of it as a startup hook.
 *
 * Why seed data?
 *   - We have no real servers to monitor
 *   - We need interesting data for Milestone 4 (Gemini querying via Text-to-SQL)
 *   - The database server is intentionally set to high CPU so queries like
 *     "which server has the highest CPU?" produce a meaningful answer
 */
@Component
@RequiredArgsConstructor  // Lombok: generates a constructor injecting all final fields
@Slf4j                    // Lombok: generates a log field backed by SLF4J
public class DataSeeder implements CommandLineRunner {

    private final ServerRepository serverRepository;
    private final MetricRepository metricRepository;

    // Fixed seed (42) = reproducible random values every restart
    private final Random random = new Random(42);

    @Override
    public void run(String... args) {
        log.info("=== Seeding infrastructure telemetry data ===");

        List<Server> servers = List.of(
            Server.builder().name("web-server-1")   .type("web")             .region("us-east-1").build(),
            Server.builder().name("db-server-1")    .type("database")        .region("us-east-1").build(),
            Server.builder().name("cache-server-1") .type("cache")           .region("us-east-1").build(),
            Server.builder().name("api-gateway-1")  .type("api-gateway")     .region("eu-west-1").build(),
            Server.builder().name("msg-broker-1")   .type("message-broker")  .region("eu-west-1").build()
        );

        List<Server> savedServers = serverRepository.saveAll(servers);
        log.info("Saved {} servers", savedServers.size());

        List<Metric> metrics = new ArrayList<>();
        for (Server server : savedServers) {
            for (int hoursAgo = 23; hoursAgo >= 0; hoursAgo--) {
                metrics.add(Metric.builder()
                    .serverId(server.getId())
                    .recordedAt(LocalDateTime.now().minusHours(hoursAgo))
                    .cpuPercent(generateCpu(server.getType()))
                    .memoryPercent(50 + random.nextDouble() * 30)  // 50–80%
                    .diskPercent(30 + random.nextDouble() * 50)    // 30–80%
                    .build());
            }
        }

        metricRepository.saveAll(metrics);
        log.info("Saved {} metric records ({} servers × 24 hours)", metrics.size(), savedServers.size());
        log.info("=== H2 Console: http://localhost:8080/h2-console ===");
        log.info("=== JDBC URL:    jdbc:h2:mem:askinfradb          ===");
    }

    /**
     * Database servers deliberately run hot (70–95% CPU).
     * This makes Milestone 4 TEXT-TO-SQL queries return interesting results:
     *   "Which server has the highest average CPU?" → db-server-1
     */
    private double generateCpu(String serverType) {
        return switch (serverType) {
            case "database"       -> 70 + random.nextDouble() * 25;  // 70–95% — runs hot
            case "cache"          -> 10 + random.nextDouble() * 20;  // 10–30% — very light
            case "api-gateway"    -> 40 + random.nextDouble() * 30;  // 40–70% — moderate
            case "message-broker" -> 30 + random.nextDouble() * 25;  // 30–55% — moderate
            default               -> 30 + random.nextDouble() * 40;  // 30–70% — web servers
        };
    }
}
