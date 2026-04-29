package com.askinfra.backend.repositories;

import com.askinfra.backend.models.Metric;
import com.askinfra.backend.models.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA repository tests using @DataJpaTest.
 *
 * @DataJpaTest is a "slice test" — it boots only the JPA layer (entities,
 * repositories, H2) without starting the full Spring application context.
 * This makes tests fast (~1–2s) and focused.
 *
 * Note: DataSeeder does NOT run here because @DataJpaTest does not start
 * CommandLineRunner beans. Each test manages its own data — this is intentional
 * to keep tests isolated and deterministic.
 */
@DataJpaTest
class MetricRepositoryTest {

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Test
    void findByServerId_returnsOnlyMetricsForThatServer() {
        // Arrange — save a server, then save one metric for it and one for a different server
        Server server = serverRepository.save(
            Server.builder().name("test-server").type("web").region("us-east-1").build()
        );

        metricRepository.save(Metric.builder()
            .serverId(server.getId())
            .recordedAt(LocalDateTime.now())
            .cpuPercent(75.0)
            .memoryPercent(60.0)
            .diskPercent(40.0)
            .build());

        // This metric belongs to a different (non-existent) server — should NOT appear
        metricRepository.save(Metric.builder()
            .serverId(9999L)
            .recordedAt(LocalDateTime.now())
            .cpuPercent(10.0)
            .memoryPercent(20.0)
            .diskPercent(30.0)
            .build());

        // Act
        List<Metric> results = metricRepository.findByServerId(server.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCpuPercent()).isEqualTo(75.0);
        assertThat(results.get(0).getServerId()).isEqualTo(server.getId());
    }

    @Test
    void findByServerId_returnsAllMetricsForThatServer() {
        // Arrange — multiple metrics for the same server
        Server server = serverRepository.save(
            Server.builder().name("multi-metric-server").type("database").region("us-east-1").build()
        );

        for (int i = 0; i < 5; i++) {
            metricRepository.save(Metric.builder()
                .serverId(server.getId())
                .recordedAt(LocalDateTime.now().minusHours(i))
                .cpuPercent(80.0 + i)
                .memoryPercent(60.0)
                .diskPercent(50.0)
                .build());
        }

        // Act
        List<Metric> results = metricRepository.findByServerId(server.getId());

        // Assert
        assertThat(results).hasSize(5);
    }

    @Test
    void findByServerId_returnsEmpty_whenNoMetricsExistForServer() {
        List<Metric> results = metricRepository.findByServerId(9999L);
        assertThat(results).isEmpty();
    }
}
