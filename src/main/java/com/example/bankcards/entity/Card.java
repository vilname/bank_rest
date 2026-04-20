package com.example.bankcards.entity;

import com.example.bankcards.util.enums.CardStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
public class Card {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private Integer number;

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

    @OneToMany(mappedBy = "cardFrom", cascade = CascadeType.MERGE)
    private Set<Transfer> writeDowns = new HashSet<>();

    @OneToMany(mappedBy = "cardTo", cascade = CascadeType.MERGE)
    private Set<Transfer> replenishment = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
