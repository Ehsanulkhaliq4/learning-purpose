package com.learningpurpose.aichatservice.repository.postgres;

import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChatMemberRepository extends R2dbcRepository<ChatMember, UUID> {

    Mono<ChatMember> findByConversationIdAndUserId(UUID conversationId, Long userId);

    Flux<ChatMember> findByConversationIdAndStatus(UUID conversationId, String status);

    Flux<ChatMember> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT user_id FROM chat_members " +
            "WHERE conversation_id = :cid AND status = 'active' AND user_id <> :exclude")
    Flux<Long> findActiveUserIdsExcept(@Param("cid") UUID cid,
                                       @Param("exclude") Long exclude);

    @Query("SELECT user_id FROM chat_members " +
            "WHERE conversation_id = :cid AND role IN ('owner','admin') AND status = 'active'")
    Flux<Long> findAdminUserIds(@Param("cid") UUID cid);

    @Query("SELECT COUNT(*) FROM chat_members " +
            "WHERE conversation_id = :cid AND status = 'active'")
    Mono<Long> countActive(@Param("cid") UUID cid);

    @Query("UPDATE chat_members SET unread_count = unread_count + 1 " +
            "WHERE conversation_id = :cid AND user_id = :uid AND status = 'active'")
    Mono<Integer> incrementUnread(@Param("cid") UUID cid, @Param("uid") Long uid);

    @Query("UPDATE chat_members SET unread_count = 0, " +
            "last_read_message_id = :mid, last_read_at = NOW() " +
            "WHERE conversation_id = :cid AND user_id = :uid")
    Mono<Integer> markRead(@Param("cid") UUID cid,
                           @Param("uid") Long uid,
                           @Param("mid") UUID messageId);
}