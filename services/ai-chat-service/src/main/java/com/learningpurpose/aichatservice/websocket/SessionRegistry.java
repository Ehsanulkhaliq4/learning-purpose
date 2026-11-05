package com.learningpurpose.aichatservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionRegistry {

    public record SessionEntry(WebSocketSession session, Sinks.Many<String> sink){}

    private final Map<Long, Map<String, SessionEntry>> sessionsByUser = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session, Sinks.Many<String> sink){
        sessionsByUser.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(session.getId(),new SessionEntry(session,sink));
        log.debug("Registered WS session {} for user {} (total sessions: {})",
                session.getId(), userId, sessionsByUser.get(userId).size());
    }

    public void unregister(Long userId, WebSocketSession session){
        var userSessions = sessionsByUser.get(userId);
        if (userSessions != null){
            userSessions.remove(session.getId());
            if (userSessions.isEmpty()) sessionsByUser.remove(userId);
        }
        log.debug("Unregistered WS session {} for user {}", session.getId(), userId);
    }

    public boolean isOnline(Long userId){
        var s = sessionsByUser.get(userId);
        return s != null && !s.isEmpty();
    }

    public Mono<Void> sendToUser(Long userId, String json){
        var userSessions = sessionsByUser.get(userId);
        if (userSessions == null || userSessions.isEmpty()) return Mono.empty();
        return Flux.fromIterable(userSessions.values())
                .doOnNext(entry -> {
                    Sinks.EmitResult result = entry.sink().tryEmitNext(json);
                    if (result.isFailure()){
                        log.warn("Sink emit failed for user {} session {}: {}",
                                userId, entry.session().getId(), result);
                    }
                }).then();
    }

    public int onlineUserCount() {
        return sessionsByUser.size();
    }
}
