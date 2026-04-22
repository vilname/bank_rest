package com.example.bankcards.service.admin.card;

import com.example.bankcards.dto.admin.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CardPanCodec;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardPanCodec cardPanCodec;

    @InjectMocks
    private AdminCardService adminCardService;

    @Test
    void createThrowsWhenCardNumberInvalid() {
        CreateCardRequest request = new CreateCardRequest(
                UUID.randomUUID(),
                "12AB",
                "John Doe",
                LocalDate.now().plusYears(1),
                100
        );
        when(userRepository.findById(request.userId())).thenReturn(Optional.of(new User()));
        when(cardPanCodec.normalize("12AB")).thenReturn("12");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> adminCardService.create(request));

        assertEquals("Card number must contain 12-19 digits", ex.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateThrowsWhenCardExpired() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);
        card.setExpiryDate(LocalDate.now().minusDays(1));
        card.setStatus(CardStatusEnum.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> adminCardService.activate(cardId));

        assertEquals("Card is expired", ex.getMessage());
        assertEquals(CardStatusEnum.DEADLINE_EXPIRED, card.getStatus());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteThrowsWhenCardNotFound() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsById(cardId)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> adminCardService.delete(cardId));

        assertEquals("Card not found", ex.getMessage());
        verify(cardRepository, never()).deleteById(cardId);
    }
}
