package com.seasonthon.everflow.app.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${cloud.aws.region.static:ap-northeast-2}") String region,
            @Value("${cloud.aws.credentials.access-key:}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key:}") String secretKey,
            @Value("${cloud.aws.credentials.profile:}") String profile
    ) {
        AwsCredentialsProvider provider;
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            provider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        } else if (!profile.isBlank()) {
            provider = ProfileCredentialsProvider.create(profile);
        } else {
            provider = DefaultCredentialsProvider.create();
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(provider)
                .build();
    }
}
