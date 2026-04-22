package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    boolean existsByPanHmac(String panHmac);

    @Query(value = """
        select c.* from "card" c
        offset :offset limit :limit
        """, nativeQuery = true)
    List<Card> findAllByPagination(@Param("offset") long offset, @Param("limit") int limit);

    @Query(value = """
        select c.* from "card" c
        where c.user_id = cast(:userId as uuid)
        and (
            not cast(:hasNorm as boolean)
            or (length(trim(cast(:norm as text))) <= 4
                and c.pan_last_four like concat('%', trim(cast(:norm as text)), '%'))
            or (length(trim(cast(:norm as text))) > 4
                and c.pan_hmac = trim(cast(:hmac as text)))
        )
        offset :offset limit :limit
        """, nativeQuery = true)
    List<Card> findByUserIdAndNumberAndPagination(
            @Param("userId") UUID userId,
            @Param("hasNorm") boolean hasNorm,
            @Param("norm") String norm,
            @Param("hmac") String hmac,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
