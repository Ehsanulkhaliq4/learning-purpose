package com.learningpurpose.aichatservice.repository.postgres;

import com.learningpurpose.aichatservice.entity.postgres.DmPair;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DmPairRepository extends R2dbcRepository<DmPair, UUID> {

    @Query("SELECT * FROM dm_pairs " +
            "WHERE (user_a_id = :a AND user_b_id = :b) " +
            "   OR (user_a_id = :b AND user_b_id = :a)")
    Mono<DmPair> findBetween(@Param("a") Long a, @Param("b") Long b);
}