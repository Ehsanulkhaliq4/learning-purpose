package com.learningpurpose.platformopsservice.controller;

import com.learningpurpose.platformopsservice.service.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ops")
@RequiredArgsConstructor
public class PlatformOpsController {

    private final KafkaOpsService kafkaOpsService;
    private final DatabaseSchemaOpsService databaseSchemaOpsService;
    private final MinioOpsService minioOpsService;
    private final MailOpsService mailOpsService;

    // --- Kafka Operations ---
    @GetMapping("/kafka/topics")
    public ResponseEntity<List<String>> listKafkaTopics() throws Exception {
        return ResponseEntity.ok(kafkaOpsService.listTopics());
    }

    @GetMapping("/kafka/topics/{topic}/tail")
    public ResponseEntity<List<KafkaOpsService.KafkaMessageRecord>> tailKafkaTopic(
            @PathVariable String topic,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(kafkaOpsService.tailTopicLogs(topic, limit));
    }

    // --- Database Schema Operations ---
    @GetMapping("/database/tables")
    public ResponseEntity<List<String>> listTables() {
        return ResponseEntity.ok(databaseSchemaOpsService.listTables());
    }

    @GetMapping("/database/tables/{tableName}/schema")
    public ResponseEntity<List<DatabaseSchemaOpsService.ColumnDefinition>> getTableSchema(@PathVariable String tableName) {
        return ResponseEntity.ok(databaseSchemaOpsService.getTableSchema(tableName));
    }

    @PostMapping("/database/ddl")
    public ResponseEntity<DatabaseSchemaOpsService.DdlResult> executeDdl(@RequestBody Map<String, String> payload) {
        String ddl = payload.get("ddl");
        if (ddl == null || ddl.isBlank()) {
            throw new IllegalArgumentException("DDL statement cannot be empty");
        }
        return ResponseEntity.ok(databaseSchemaOpsService.executeControlledDdl(ddl));
    }

    // --- MinIO Storage Telemetry ---
    @GetMapping("/minio/buckets")
    public ResponseEntity<List<MinioOpsService.BucketTelemetry>> getMinioBuckets() throws Exception {
        return ResponseEntity.ok(minioOpsService.getBucketsTelemetry());
    }

    @GetMapping("/minio/buckets/{bucketName}/objects")
    public ResponseEntity<List<MinioOpsService.ObjectMeta>> listBucketObjects(@PathVariable String bucketName) throws Exception {
        return ResponseEntity.ok(minioOpsService.listBucketObjects(bucketName));
    }

    // --- Mail Diagnostics ---
    @GetMapping("/mail/probe")
    public ResponseEntity<MailOpsService.MailStatusReport> probeMailServer() {
        return ResponseEntity.ok(mailOpsService.testConnection());
    }

    @PostMapping("/mail/probe/send")
    public ResponseEntity<MailOpsService.MailStatusReport> sendTestEmail(@RequestParam @NotBlank String recipient) {
        return ResponseEntity.ok(mailOpsService.sendDiagnosticProbe(recipient));
    }
}
