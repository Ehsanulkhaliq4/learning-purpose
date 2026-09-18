package com.learningpurpose.platformopsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaOpsService {

    private final AdminClient adminClient;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public List<String> listTopics() throws Exception {
        return adminClient.listTopics().names().get(5, TimeUnit.SECONDS).stream().sorted().toList();
    }

    public List<KafkaMessageRecord> tailTopicLogs(String topic, int limit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ops-log-tailer-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        List<KafkaMessageRecord> records = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            var partitions = consumer.partitionsFor(topic);
            if (partitions == null || partitions.isEmpty()) {
                return Collections.emptyList();
            }

            List<TopicPartition> topicPartitions = partitions.stream()
                    .map(p -> new TopicPartition(p.topic(), p.partition()))
                    .toList();

            consumer.assign(topicPartitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);

            // Seek backwards by `limit` records across partitions
            for (TopicPartition tp : topicPartitions) {
                long endOffset = endOffsets.getOrDefault(tp, 0L);
                long startSeek = Math.max(0, endOffset - limit);
                consumer.seek(tp, startSeek);
            }

            ConsumerRecords<String, String> polled = consumer.poll(Duration.ofSeconds(3));
            for (ConsumerRecord<String, String> rec : polled) {
                records.add(new KafkaMessageRecord(
                        rec.topic(),
                        rec.partition(),
                        rec.offset(),
                        rec.key(),
                        rec.value(),
                        Instant.ofEpochMilli(rec.timestamp()).toString()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to tail Kafka topic [{}]", topic, e);
            throw new RuntimeException("Error reading topic logs: " + e.getMessage(), e);
        }

        // Return latest messages first
        records.sort((a, b) -> Long.compare(b.offset(), a.offset()));
        return records.stream().limit(limit).toList();
    }

    public record KafkaMessageRecord(String topic, int partition, long offset, String key, String payload, String timestamp) {}
}
