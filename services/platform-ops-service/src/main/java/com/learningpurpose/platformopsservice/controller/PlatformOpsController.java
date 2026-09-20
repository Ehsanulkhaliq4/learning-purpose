package com.learningpurpose.platformopsservice.controller;

import com.learningpurpose.platformopsservice.dto.ColumnDefinition;
import com.learningpurpose.platformopsservice.dto.DdlResult;
import com.learningpurpose.platformopsservice.dto.TablePage;
import com.learningpurpose.platformopsservice.service.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final UserDirectoryService userDirectoryService;
    private final MailpitInboxService mailpitInboxService;

    /* ── Kafka ── */
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

    /* ── Database ── */

    /** List all databases this ops service is allowed to manage. */
    @GetMapping("/database/list")
    public ResponseEntity<List<String>> listDatabases() {
        return ResponseEntity.ok(databaseSchemaOpsService.listDatabases());
    }

    /** List tables in the chosen database. */
    @GetMapping("/database/{db}/tables")
    public ResponseEntity<List<String>> listTables(@PathVariable String db) {
        return ResponseEntity.ok(databaseSchemaOpsService.listTables(db));
    }

    /** Get schema of a table in the chosen database. */
    @GetMapping("/database/{db}/tables/{tableName}/schema")
    public ResponseEntity<List<ColumnDefinition>> getTableSchema(
            @PathVariable String db, @PathVariable String tableName) {
        return ResponseEntity.ok(databaseSchemaOpsService.getTableSchema(db, tableName));
    }

    /** Execute a controlled DDL statement against the chosen database. */
    @PostMapping("/database/{db}/ddl")
    public ResponseEntity<DdlResult> executeDdl(
            @PathVariable String db, @RequestBody Map<String, String> payload) {
        String ddl = payload.get("ddl");
        if (ddl == null || ddl.isBlank()) {
            throw new IllegalArgumentException("DDL statement cannot be empty");
        }
        return ResponseEntity.ok(databaseSchemaOpsService.executeControlledDdl(db, ddl));
    }

    @GetMapping("/database/{db}/tables/{tableName}/rows")
    public ResponseEntity<TablePage> listRows(
            @PathVariable String db,
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false)    String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(
                databaseSchemaOpsService.listRows(db, tableName, page, size, sortBy, sortDir));
    }


    /* ── MinIO ── */
    @GetMapping("/minio/buckets")
    public ResponseEntity<List<MinioOpsService.BucketTelemetry>> getMinioBuckets() throws Exception {
        return ResponseEntity.ok(minioOpsService.getBucketsTelemetry());
    }

    @GetMapping("/minio/buckets/{bucketName}/objects")
    public ResponseEntity<List<MinioOpsService.ObjectMeta>> listBucketObjects(
            @PathVariable String bucketName) throws Exception {
        return ResponseEntity.ok(minioOpsService.listBucketObjects(bucketName));
    }

    @GetMapping("/mail/probe")
    public ResponseEntity<MailOpsService.MailStatusReport> probeMailServer() {
        return ResponseEntity.ok(mailOpsService.testConnection());
    }

    @PostMapping("/mail/probe/send")
    public ResponseEntity<MailOpsService.MailStatusReport> sendTestEmail(
            @RequestParam @NotBlank String recipient) {
        return ResponseEntity.ok(mailOpsService.sendDiagnosticProbe(recipient));
    }

    /** Broadcast to every user in user_db. */
    @PostMapping("/mail/broadcast/all-users")
    public ResponseEntity<MailOpsService.BatchMailReport> broadcastToAllUsers(
            @RequestParam @NotBlank String subject,
            @RequestParam @NotBlank String htmlBody,
            @RequestParam(defaultValue = "50") int batchSize) {

        List<String> recipients = userDirectoryService.getAllUserEmails();
        return ResponseEntity.ok(
                mailOpsService.sendBatch(recipients, subject, htmlBody, batchSize));
    }

    /** List all emails currently sitting in Mailpit. */
    @GetMapping("/mail/inbox")
    public ResponseEntity<MailpitInboxService.InboxPage> listInbox(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mailpitInboxService.listMessages(page, size));
    }

    /** Get full details of one email (headers, body, attachments). */
    @GetMapping("/mail/inbox/{id}")
    public ResponseEntity<Map<String, Object>> getInboxMessage(@PathVariable String id) {
        return ResponseEntity.ok(mailpitInboxService.getMessage(id));
    }

    /** Raw HTML body of one email — useful in browser. */
    @GetMapping(value = "/mail/inbox/{id}/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getInboxMessageHtml(@PathVariable String id) {
        String html = mailpitInboxService.getHtmlBody(id);
        if (html == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<p>This message has no HTML part.</p>");
        }
        return ResponseEntity.ok(html);
    }

    /** Raw text body of one email. */
    @GetMapping(value = "/mail/inbox/{id}/text", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getInboxMessageText(@PathVariable String id) {
        String text = mailpitInboxService.getTextBody(id);
        if (text == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("This message has no plain-text part.");
        }
        return ResponseEntity.ok(text);
    }

    /** Wipe the inbox (dev-only helper). */
    @DeleteMapping("/mail/inbox")
    public ResponseEntity<Void> purgeInbox() {
        mailpitInboxService.purgeInbox();
        return ResponseEntity.noContent().build();
    }
}