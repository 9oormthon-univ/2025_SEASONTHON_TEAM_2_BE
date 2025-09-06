package com.seasonthon.everflow.app.s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFilename)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
        return getFileUrl(uniqueFilename);
    }

    public String uploadWithPrefix(String prefix, MultipartFile multipartFile) {
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String base = originalFilename == null ? "file" : originalFilename;
            String uniqueFilename = prefix + UUID.randomUUID() + "_" + base;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFilename)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            return getFileUrl(uniqueFilename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        String key = extractKeyFromUrl(fileUrl);
        if (key != null && !key.isBlank()) {
            deleteFile(key);
        }
    }

    private String getFileUrl(String key) {
        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.utilities().getUrl(getUrlRequest).toExternalForm();
    }

    private String extractKeyFromUrl(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        if (idx > 0) {
            return url.substring(idx + ".amazonaws.com/".length() + url.substring(0, idx).lastIndexOf('/') - url.substring(0, idx).length());
        }
        int pos = url.indexOf(bucketName + "/");
        if (pos >= 0) return url.substring(pos + (bucketName + "/").length());
        return null;
    }
}
