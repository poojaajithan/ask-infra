package com.askinfra.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the full Spring context loads without errors.
 * If wiring is broken (missing beans, config errors), this test fails first.
 */
@SpringBootTest
class AskInfraApplicationTests {

    @Test
    void contextLoads() {
        // Passes if the ApplicationContext starts without throwing
    }
}
