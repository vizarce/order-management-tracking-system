package com.ordertracking.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Verifies that the Spring application context loads correctly.
 *
 * <p>External infrastructure (Kafka) is not available in CI, so
 * {@code KafkaAutoConfiguration} is excluded via
 * {@code src/test/resources/application.yml}. A {@link KafkaTemplate} mock is
 * registered so that {@code OrderEventProducer} can be wired without a real broker.</p>
 *
 * <p>The datasource is backed by H2 in-memory, as configured in
 * {@code src/test/resources/application.yml}.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderServiceApplicationTests {

    /** Satisfies {@code OrderEventProducer}'s dependency when Kafka auto-config is excluded. */
    @MockBean
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void contextLoads() {}
}
