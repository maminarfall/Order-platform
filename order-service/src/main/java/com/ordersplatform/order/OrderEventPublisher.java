package com.ordersplatform.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Map;

@Component
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final SnsClient sns;
    private final ObjectMapper mapper;
    private final String topicArn;

    public OrderEventPublisher(SnsClient sns, ObjectMapper mapper, @Value("${orders.topic-arn}") String topicArn) {
        this.sns = sns;
        this.mapper = mapper;
        this.topicArn = topicArn;
    }

    public void publishOrderCreated(Order order) {
        try {
            String body = mapper.writeValueAsString(order);
            sns.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(body)
                    .messageAttributes(Map.of("eventType",
                            MessageAttributeValue.builder().dataType("String").stringValue("OrderCreated").build()))
                    .build());
            log.info("Published OrderCreated event for order {}", order.id());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish OrderCreated event", e);
        }
    }
}
