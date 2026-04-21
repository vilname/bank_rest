package com.example.bankcards.service.api.card;

import com.example.bankcards.dto.api.card.BalanceResponse;
import com.example.bankcards.dto.api.card.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCardSavesNewCardWithDefaultStatus() {
        User user = new User();
        user.setId(UUID.randomUUID());

        CardRequest request = new CardRequest();
        request.setNumber("1234567812345678");
        request.setBalance(500);
        request.setOwner("John Doe");

        when(cardRepository.existsByNumber(request.getNumber())).thenReturn(false);

        cardService.createCard(user, request);

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCardThrowsWhenCardNumberExists() {
        User user = new User();
        CardRequest request = new CardRequest();
        request.setNumber("1234567812345678");
        request.setBalance(500);
        request.setOwner("John Doe");

        when(cardRepository.existsByNumber(request.getNumber())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> cardService.createCard(user, request));

        assertEquals("Card number already exists", ex.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getCardBalanceReturnsMaskedNumberForOwner() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setNumber("1234567812345678");
        card.setBalance(1000);
        card.setStatus(CardStatusEnum.ACTIVE);
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BalanceResponse response = cardService.getCardBalance(user, cardId);

        assertEquals(cardId, response.getCardId());
        assertEquals("**** **** **** 5678", response.getCardNumber());
        assertEquals(1000, response.getBalance());
    }

    @Test
    void deleteCardThrowsWhenBalancePositive() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setUser(user);
        card.setBalance(1);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        BusinessException ex = assertThrows(BusinessException.class, () -> cardService.deleteCard(user, card.getId()));

        assertTrue(ex.getMessage().contains("Cannot delete card with positive balance"));
        verify(cardRepository, never()).delete(any(Card.class));
    }
}
