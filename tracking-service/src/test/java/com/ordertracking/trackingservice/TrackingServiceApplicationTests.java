package com.ordertracking.trackingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
    "spring.data.redis.host=localhost",
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.ReactiveMongoRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration"
})
class TrackingServiceApplicationTests {
    @Test
    void contextLoads() {}
}
