package com.learningpurpose.aichatservice.websocket;

import com.learningpurpose.aichatservice.dto.ClientMessage;
import com.learningpurpose.aichatservice.dto.ServerMessage;
import com.learningpurpose.aichatservice.entity.cassandra.ChatMessage;
import com.learningpurpose.aichatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final SessionRegistry registry;
    private final ChatService chatService;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Long userId = UserIdExtractor.extract(session);
        if (userId == null) {
            log.warn("WS connection rejected — no user id in handshake");
            return session.close();
        }

        // Sink for all outbound frames to this session (serializes sends)
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        registry.register(userId, session, sink);

        // INBOUND — client messages
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .concatMap(payload -> handleMessage(userId, session, payload))
                .doOnError(err -> log.warn("WS input error for {}: {}", userId, err.getMessage()))
                .doOnComplete(sink::tryEmitComplete)
                .then();

        // OUTBOUND — everything emitted by producers via SessionRegistry
        Mono<Void> output = session.send(sink.asFlux().map(session::textMessage));

        return Mono.zip(input, output)
                .doFinally(sig -> registry.unregister(userId, session))
                .then();
    }

    private Mono<Void> handleMessage(Long userId, WebSocketSession session, String payload) {
        ClientMessage msg;
        try {
            msg = mapper.readValue(payload, ClientMessage.class);
        } catch (Exception e) {
            return sendError(session, "Invalid JSON: " + e.getMessage());
        }

        if (msg.getType() == null) return sendError(session, "Missing type");

        return switch (msg.getType()) {
            case "SEND" -> chatService.sendMessage(userId, msg)
                    .onErrorResume(err ->
                            sendError(session, err.getMessage())
                                    .then(Mono.<ChatMessage>empty()))
                    .then();
            case "READ" -> chatService.markRead(userId, msg)
                    .onErrorResume(err -> sendError(session, err.getMessage()))
                    .then();
            case "PING" -> sendPong(session);
            default -> sendError(session, "Unknown type: " + msg.getType());
        };
    }

    private Mono<Void> sendError(WebSocketSession session, String message) {
        try {
            String json = mapper.writeValueAsString(ServerMessage.builder()
                    .type("ERROR").content(message).createdAt(Instant.now()).build());
            return session.send(Mono.just(session.textMessage(json)));
        } catch (Exception e) {
            return session.close();
        }
    }

    private Mono<Void> sendPong(WebSocketSession session) {
        try {
            String json = mapper.writeValueAsString(ServerMessage.builder()
                    .type("PONG").createdAt(Instant.now()).build());
            return session.send(Mono.just(session.textMessage(json)));
        } catch (Exception e) {
            return session.close();
        }
    }
}
