package com.ordertracking.trackingservice.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Reactive Redis configuration.
 * <p>
 * The cache stores {@link OrderTrackingDto} values only.  Using a typed
 * {@link Jackson2JsonRedisSerializer} (rather than the generic variant with
 * {@code DefaultTyping}) avoids the need to embed class names in the JSON and
 * therefore eliminates polymorphic-deserialization attack vectors entirely.
 */
@Configuration
public class ReactiveRedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, OrderTrackingDto> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<OrderTrackingDto> jsonSerializer =
            new Jackson2JsonRedisSerializer<>(mapper, OrderTrackingDto.class);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisSerializationContext<String, OrderTrackingDto> context =
            RedisSerializationContext.<String, OrderTrackingDto>newSerializationContext(stringSerializer)
                .value(jsonSerializer)
                .hashKey(stringSerializer)
                .hashValue(jsonSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
