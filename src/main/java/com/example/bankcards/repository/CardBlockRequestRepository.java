package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.util.enums.CardBlockRequestStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, UUID> {
    boolean existsByCardIdAndUserIdAndStatus(
            UUID cardId,
            UUID userId,
            CardBlockRequestStatusEnum status
    );

    @Query(value = """
        select r.* from card_block_request r
        where r.status = cast(:status as varchar)
        order by r.created desc
        offset :offset limit :limit
        """, nativeQuery = true)
    List<CardBlockRequest> findByStatusWithPagination(
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countByStatus(CardBlockRequestStatusEnum status);
}
