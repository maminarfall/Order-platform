package com.ordersplatform.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Polls the SQS queue (subscribed to the SNS topic) and "sends" a notification.
 * Messages that fail are NOT deleted, so SQS retries them and eventually moves them to the DLQ.
 */
@Component
public class OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final SqsClient sqs;
    private final ObjectMapper mapper;
    private final String queueUrl;
    private final Counter processed;
    private final Counter failed;

    public OrderEventListener(SqsClient sqs, ObjectMapper mapper, MeterRegistry registry,
                              @Value("${orders.queue-url}") String queueUrl) {
        this.sqs = sqs;
        this.mapper = mapper;
        this.queueUrl = queueUrl;
        this.processed = Counter.builder("notifications_processed_total").register(registry);
        this.failed = Counter.builder("notifications_failed_total").register(registry);
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        var messages = sqs.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10) // long polling = fewer empty calls, lower cost
                .build()).messages();

        for (Message m : messages) {
            try {
                handle(m.body());
                sqs.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(m.receiptHandle()).build());
                processed.increment();
            } catch (Exception e) {
                failed.increment();
                log.error("Failed to process message {}, leaving it for retry", m.messageId(), e);
            }
        }
    }

    void handle(String body) throws Exception {
        JsonNode node = mapper.readTree(body);
        // SNS wraps the payload in an envelope unless raw delivery is enabled
        JsonNode order = node.has("Message") ? mapper.readTree(node.get("Message").asText()) : node;
        log.info("NOTIFICATION -> to={} : Your order {} for {} x {} was received",
                order.path("customerEmail").asText("unknown"),
                order.path("id").asText(),
                order.path("quantity").asInt(),
                order.path("item").asText());
    }
}
