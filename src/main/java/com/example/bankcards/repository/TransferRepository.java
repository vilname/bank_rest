package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Query(value = """
        select t.* from "transfer" t 
        join card cardFrom on cardFrom.id = t.card_from_id
        join card cardTo on cardTo.id = t.card_to_id
        where cardFrom.user_id=:userId and cardTo.user_id=:userId
        offset :offset limit :limit
        """, nativeQuery = true)
    List<Transfer> findUserTransfersWithPagination(
            @Param("userId") UUID userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
