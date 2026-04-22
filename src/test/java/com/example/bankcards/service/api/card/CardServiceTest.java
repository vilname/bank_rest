package com.example.bankcards.service.api.card;

import com.example.bankcards.dto.api.card.BalanceResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CardPanCodec;
import com.example.bankcards.util.enums.CardBlockRequestStatusEnum;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardBlockRequestRepository cardBlockRequestRepository;
    @Mock
    private CardPanCodec cardPanCodec;

    @InjectMocks
    private CardService cardService;

    @Test
    void getCardBalanceReturnsMaskedNumberForOwner() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setBalance(1000);
        card.setStatus(CardStatusEnum.ACTIVE);
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardPanCodec.readPlainPan(card)).thenReturn("1234567812345678");

        BalanceResponse response = cardService.getCardBalance(user, cardId);

        assertEquals(cardId, response.getCardId());
        assertEquals("**** **** **** 5678", response.getCardNumber());
        assertEquals(1000, response.getBalance());
    }

    @Test
    void blockCardCreatesPendingRequest() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setUser(user);
        card.setStatus(CardStatusEnum.ACTIVE);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardBlockRequestRepository.existsByCardIdAndUserIdAndStatus(
                card.getId(), user.getId(), CardBlockRequestStatusEnum.PENDING
        )).thenReturn(false);

        cardService.blockCard(user, card.getId());

        verify(cardBlockRequestRepository).save(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCardThrowsWhenRequestAlreadyPending() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setUser(user);
        card.setStatus(CardStatusEnum.ACTIVE);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardBlockRequestRepository.existsByCardIdAndUserIdAndStatus(
                card.getId(), user.getId(), CardBlockRequestStatusEnum.PENDING
        )).thenReturn(true);

        assertThrows(BadRequestException.class, () -> cardService.blockCard(user, card.getId()));
        verify(cardBlockRequestRepository, never()).save(any());
    }

    @Test
    void blockCardThrowsWhenAlreadyBlocked() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setUser(user);
        card.setStatus(CardStatusEnum.BLOCKED);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> cardService.blockCard(user, card.getId()));

        verify(cardBlockRequestRepository, never()).save(any());
    }
}
