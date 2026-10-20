package com.ordersplatform.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;

import java.net.URI;

@Configuration
public class AwsConfig {
    // AWS_ENDPOINT is only set locally (LocalStack). On EKS it is empty and the real AWS endpoint is used.
    @Bean
    SnsClient snsClient(@Value("${aws.region}") String region, @Value("${aws.endpoint:}") String endpoint) {
        SnsClientBuilder b = SnsClient.builder().region(Region.of(region));
        if (!endpoint.isBlank()) b.endpointOverride(URI.create(endpoint));
        return b.build();
    }
}
