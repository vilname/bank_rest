package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = """
        select distinct u.* from "user" u 
        join user_role ur ON u.id = ur.user_id 
        join role r ON ur.role_id = r.id
        offset :offset limit :limit
        """, nativeQuery = true)
    List<User> findAllWithPaginationAndRoles(@Param("offset") long offset, @Param("limit") int limit);

}

