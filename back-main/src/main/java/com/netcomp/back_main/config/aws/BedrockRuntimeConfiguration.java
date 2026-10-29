package com.netcomp.back_main.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.StandardRetryStrategy;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockRuntimeConfiguration {

    @Bean(destroyMethod = "close")
    public BedrockRuntimeClient bedrockRuntimeClient(Environment environment) {
        AwsCredentialsProvider credentialsProvider = () -> {
            String accessKeyId = environment.getProperty("AWS_ACCESS_KEY_ID");
            String secretAccessKey = environment.getProperty("AWS_SECRET_ACCESS_KEY");
            if (accessKeyId == null || accessKeyId.isBlank()
                    || secretAccessKey == null || secretAccessKey.isBlank()) {
                throw new IllegalStateException("AWS credentials are not configured.");
            }
            return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        };

        return BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryStrategy(StandardRetryStrategy.builder()
                                .maxAttempts(1)
                                .build())
                        .build())
                .build();
    }
}
