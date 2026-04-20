package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer")
@Getter
@Setter
@NoArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private Integer balance;

    @CreationTimestamp
    private Instant created;

    @ManyToOne()
    @JoinColumn(name = "card_from_id", nullable = false)
    private Card cardFrom;

    @ManyToOne()
    @JoinColumn(name = "card_to_id", nullable = false)
    private Card cardTo;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
