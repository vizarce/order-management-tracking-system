package com.ordertracking.orderservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordertracking.common.mdc.MdcConstants;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.OrderItem;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.infrastructure.kafka.producer.OrderEventProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;

    private OrderEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new OrderEventProducer(kafkaTemplate, new ObjectMapper().registerModule(new JavaTimeModule()));
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void publishOrderCreated_propagatesMdcHeadersToKafkaMessage() {
        MDC.put(MdcConstants.TRACE_ID,   "trace-123");
        MDC.put(MdcConstants.REQUEST_ID, "req-456");
        MDC.put(MdcConstants.USER_ID,    "user-789");

        Order order = buildOrder();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Message<String>> captor = ArgumentCaptor.forClass(Message.class);

        producer.publishOrderCreated(order);

        verify(kafkaTemplate).send(captor.capture());
        Message<String> message = captor.getValue();

        assertThat(headerAsString(message, MdcConstants.HEADER_TRACE_ID)).isEqualTo("trace-123");
        assertThat(headerAsString(message, MdcConstants.HEADER_REQUEST_ID)).isEqualTo("req-456");
        assertThat(headerAsString(message, MdcConstants.HEADER_USER_ID)).isEqualTo("user-789");
    }

    @Test
    void publishOrderCreated_doesNotAddHeadersWhenMdcIsEmpty() {
        Order order = buildOrder();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Message<String>> captor = ArgumentCaptor.forClass(Message.class);

        producer.publishOrderCreated(order);

        verify(kafkaTemplate).send(captor.capture());
        Message<String> message = captor.getValue();

        assertThat(message.getHeaders().get(MdcConstants.HEADER_TRACE_ID)).isNull();
        assertThat(message.getHeaders().get(MdcConstants.HEADER_REQUEST_ID)).isNull();
        assertThat(message.getHeaders().get(MdcConstants.HEADER_USER_ID)).isNull();
    }

    @Test
    void publishOrderStatusUpdated_propagatesMdcHeadersToKafkaMessage() {
        MDC.put(MdcConstants.TRACE_ID,   "trace-upd");
        MDC.put(MdcConstants.REQUEST_ID, "req-upd");
        MDC.put(MdcConstants.USER_ID,    "user-upd");

        Order order = buildOrder();
        order.confirm();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Message<String>> captor = ArgumentCaptor.forClass(Message.class);

        producer.publishOrderStatusUpdated(order, "PENDING");

        verify(kafkaTemplate).send(captor.capture());
        Message<String> message = captor.getValue();

        assertThat(headerAsString(message, MdcConstants.HEADER_TRACE_ID)).isEqualTo("trace-upd");
        assertThat(headerAsString(message, MdcConstants.HEADER_REQUEST_ID)).isEqualTo("req-upd");
        assertThat(headerAsString(message, MdcConstants.HEADER_USER_ID)).isEqualTo("user-upd");
    }

    private Order buildOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.addItem(new OrderItem("prod-1", "Widget", 1, Money.of(BigDecimal.TEN, "USD")));
        return order;
    }

    private String headerAsString(Message<?> message, String headerName) {
        Object value = message.getHeaders().get(headerName);
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return value != null ? value.toString() : null;
    }
}
