package com.example.bankcards.service.api.transfer;

import com.example.bankcards.dto.api.transfer.TransferRequest;
import com.example.bankcards.dto.api.transfer.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void makeTransferMovesBalanceBetweenOwnActiveCards() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card fromCard = new Card();
        fromCard.setId(UUID.randomUUID());
        fromCard.setNumber("1111222233334444");
        fromCard.setBalance(1000);
        fromCard.setStatus(CardStatusEnum.ACTIVE);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(UUID.randomUUID());
        toCard.setNumber("5555666677778888");
        toCard.setBalance(200);
        toCard.setStatus(CardStatusEnum.ACTIVE);
        toCard.setUser(user);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(fromCard.getId());
        request.setToCardId(toCard.getId());
        request.setAmount(300);

        Transfer transfer = new Transfer();
        transfer.setId(UUID.randomUUID());
        transfer.setCreated(Instant.now());

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        TransferResponse response = transferService.makeTransfer(user, request);

        assertEquals(700, fromCard.getBalance());
        assertEquals(500, toCard.getBalance());
        assertEquals("**** **** **** 4444", response.getFromCardNumber());
        assertEquals("**** **** **** 8888", response.getToCardNumber());
    }

    @Test
    void makeTransferThrowsWhenCardsAreSame() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setNumber("1111222233334444");
        card.setBalance(1000);
        card.setStatus(CardStatusEnum.ACTIVE);
        card.setUser(user);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(card.getId());
        request.setToCardId(card.getId());
        request.setAmount(10);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        BusinessException ex = assertThrows(BusinessException.class, () -> transferService.makeTransfer(user, request));

        assertEquals("Cannot transfer to the same card", ex.getMessage());
    }

    @Test
    void makeTransferThrowsWhenBalanceInsufficient() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Card fromCard = new Card();
        fromCard.setId(UUID.randomUUID());
        fromCard.setNumber("1111222233334444");
        fromCard.setBalance(100);
        fromCard.setStatus(CardStatusEnum.ACTIVE);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(UUID.randomUUID());
        toCard.setNumber("5555666677778888");
        toCard.setBalance(200);
        toCard.setStatus(CardStatusEnum.ACTIVE);
        toCard.setUser(user);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(fromCard.getId());
        request.setToCardId(toCard.getId());
        request.setAmount(300);

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        BusinessException ex = assertThrows(BusinessException.class, () -> transferService.makeTransfer(user, request));

        assertTrue(ex.getMessage().contains("Insufficient balance"));
    }
}
