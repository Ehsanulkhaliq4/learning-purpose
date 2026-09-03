package com.learningpurpose.notificationservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.backoff.FixedBackOff;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Pure Apache Kafka non-deprecated serializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Non-deprecated RecordMessageConverter implementation delegating to Jackson
     */
    @Bean
    public RecordMessageConverter customRecordMessageConverter(ObjectMapper objectMapper) {
        return new RecordMessageConverter() {
            @Override
            public @NonNull Message<?> toMessage(ConsumerRecord<?, ?> record, @Nullable Object acknowledgment, @Nullable Object consumer, @Nullable Type payloadType) {
                try {
                    Object rawValue = record.value();
                    Object targetPayload;

                    if (rawValue instanceof String strValue) {
                        targetPayload = objectMapper.readValue(strValue, objectMapper.constructType(payloadType));
                    } else if (rawValue instanceof byte[] byteValue) {
                        targetPayload = objectMapper.readValue(byteValue, objectMapper.constructType(payloadType));
                    } else {
                        targetPayload = rawValue;
                    }

                    return MessageBuilder.withPayload(targetPayload)
                            .setHeader("kafka_receivedTopic", record.topic())
                            .setHeader("kafka_receivedPartitionId", record.partition())
                            .setHeader("kafka_offset", record.offset())
                            .build();
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Failed to decode JSON payload for topic " + record.topic(), ex);
                }
            }

            @Override
            public ProducerRecord<?, ?> fromMessage(Message<?> message, String defaultTopic) {
                throw new UnsupportedOperationException("Inbound consumer converter does not convert to ProducerRecord");
            }
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            RecordMessageConverter customRecordMessageConverter) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordMessageConverter(customRecordMessageConverter);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }
}