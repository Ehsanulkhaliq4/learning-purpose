package com.learningpurpose.platformopsservice.service;

import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioOpsService {

    private final MinioClient minioClient;

    public List<BucketTelemetry> getBucketsTelemetry() throws Exception {
        List<Bucket> buckets = minioClient.listBuckets();
        List<BucketTelemetry> telemetryList = new ArrayList<>();

        for (Bucket bucket : buckets) {
            String name = bucket.name();
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(name).recursive(true).build()
            );

            long totalSizeBytes = 0;
            int objectCount = 0;

            for (Result<Item> result : objects) {
                Item item = result.get();
                totalSizeBytes += item.size();
                objectCount++;
            }

            telemetryList.add(new BucketTelemetry(
                    name,
                    bucket.creationDate().toInstant().toString(),
                    objectCount,
                    totalSizeBytes,
                    totalSizeBytes / (1024.0 * 1024.0) // MB
            ));
        }

        return telemetryList;
    }

    public List<ObjectMeta> listBucketObjects(String bucketName) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build()
        );

        List<ObjectMeta> objects = new ArrayList<>();
        for (Result<Item> res : results) {
            Item item = res.get();
            objects.add(new ObjectMeta(
                    item.objectName(),
                    item.size(),
                    item.lastModified().toInstant().toString(),
                    item.etag()
            ));
        }
        return objects;
    }

    public record BucketTelemetry(String bucketName, String createdAt, int totalObjects, long totalSizeBytes, double totalSizeMb) {}
    public record ObjectMeta(String objectName, long sizeBytes, String lastModified, String etag) {}
}
