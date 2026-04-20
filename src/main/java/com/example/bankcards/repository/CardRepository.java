package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Page<Card> findAllByUserId(UUID userId, Pageable pageable);
    Page<Card> findAllByUserIdAndStatus(UUID userId, CardStatusEnum status, Pageable pageable);
    Optional<Card> findByIdAndUserId(UUID cardId, UUID userId);
    boolean existsByNumber(String number);

    @Query(value = """
        select distinct c.* from "card" c 
        offset :offset limit :limit
        """, nativeQuery = true)
    List<Card> findAllWithPaginationAndRoles(@Param("offset") long offset, @Param("limit") int limit);
}

