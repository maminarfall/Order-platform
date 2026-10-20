package com.ordersplatform.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
public class AwsConfig {
    @Bean
    SqsClient sqsClient(@Value("${aws.region}") String region, @Value("${aws.endpoint:}") String endpoint) {
        SqsClientBuilder b = SqsClient.builder().region(Region.of(region));
        if (!endpoint.isBlank()) b.endpointOverride(URI.create(endpoint));
        return b.build();
    }
}
