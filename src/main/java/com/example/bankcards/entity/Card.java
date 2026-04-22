package com.example.bankcards.entity;

import com.example.bankcards.util.enums.CardStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
public class Card {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pan_cipher", nullable = false, columnDefinition = "TEXT")
    private String panCipher;

    @Column(name = "pan_hmac", nullable = false, length = 64, unique = true)
    private String panHmac;

    @Column(name = "pan_last_four", nullable = false, length = 4)
    private String panLastFour;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false)
    private String owner;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatusEnum status;

    @CreationTimestamp
    private Instant created;

    @UpdateTimestamp
    private Instant updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
