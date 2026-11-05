package com.learningpurpose.aichatservice.repository.postgres;

import com.learningpurpose.aichatservice.entity.postgres.Invite;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface InviteRepository extends R2dbcRepository<Invite, UUID> {
    Flux<Invite> findByInviteeIdAndStatus(Long inviteeId, String status);
}