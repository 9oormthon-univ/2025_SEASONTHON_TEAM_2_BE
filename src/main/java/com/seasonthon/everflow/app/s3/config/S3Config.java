package com.seasonthon.everflow.app.s3.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${cloud.aws.region.static:ap-northeast-2}") String region,
            @Value("${cloud.aws.credentials.access-key:}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key:}") String secretKey
    ) {
        AwsCredentialsProvider provider;

        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            provider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
            log.info("S3 credentials provider: StaticCredentialsProvider");
        } else {
            provider = DefaultCredentialsProvider.create();
            log.info("S3 credentials provider: DefaultCredentialsProvider");
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(provider)
                .build();
    }
}
