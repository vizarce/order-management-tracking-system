package com.ordertracking.trackingservice;

import com.mongodb.reactivestreams.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies that the Spring application context loads correctly.
 *
 * <p>External infrastructure (Redis, MongoDB, Kafka) is not available in CI, so:</p>
 * <ul>
 *   <li>Kafka and the non-reactive Redis auto-configurations are excluded.</li>
 *   <li>{@link MongoClient} and {@link ReactiveRedisConnectionFactory} are provided as
 *       Mockito mocks. Spring Data creates the template/repository beans using these mocks;
 *       no actual network calls are made during context initialisation.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
class TrackingServiceApplicationTests {

    /** Prevents Spring Data MongoDB reactive from attempting a real connection. */
    @MockBean
    MongoClient mongoClient;

    /** Prevents Spring Data Redis reactive from attempting a real connection. */
    @MockBean
    ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Test
    void contextLoads() {}
}
