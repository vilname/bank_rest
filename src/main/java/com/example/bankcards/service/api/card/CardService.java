package com.example.bankcards.service.api.card;

import com.example.bankcards.dto.api.card.BalanceResponse;
import com.example.bankcards.dto.api.card.CardListRequest;
import com.example.bankcards.dto.api.card.CardRequest;
import com.example.bankcards.dto.api.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.enums.CardStatusEnum;
import com.example.bankcards.util.helper.CardMaskerHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PaginationResponse<CardResponse> getUserCards(User authUser, CardListRequest cardListRequest) {

        List<Card> cards = cardRepository.findByUserIdAndNumberAndPagination(
                authUser.getId(),
                cardListRequest.getNumber(),
                cardListRequest.getPagination().getOffset(),
                cardListRequest.getPagination().getLimit()
            );

        List<CardResponse> cardList = cards.stream().map(CardService::toDto).toList();

        int total = (int)cardRepository.count();

        return new PaginationResponse<>(cardList, cardListRequest.getPagination(), total);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getCardBalance(User user, UUID cardId) {
        Card card = getCardAndValidateOwnership(cardId, user);

        return BalanceResponse.builder()
                .cardId(card.getId())
                .cardNumber(CardMaskerHelper.mask(card.getNumber()))
                .balance(card.getBalance())
                .status(card.getStatus().toString())
                .build();
    }

    @Transactional
    public void createCard(User user, CardRequest request) {
        if (cardRepository.existsByNumber(request.getNumber())) {
            throw new BusinessException("Card number already exists");
        }

        Card card = new Card();
        card.setNumber(request.getNumber());
        card.setBalance(request.getBalance());
        card.setOwner(request.getOwner());
        card.setExpiryDate(LocalDate.now().plusYears(4));
        card.setStatus(CardStatusEnum.ACTIVE);
        card.setUser(user);

        cardRepository.save(card);
    }

    @Transactional
    public void updateCard(User user, UUID cardId, CardRequest request) {
        Card card = getCardAndValidateOwnership(cardId, user);

        if (!card.getNumber().equals(request.getNumber()) &&
                cardRepository.existsByNumber(request.getNumber())) {
            throw new BusinessException("Card number already exists");
        }

        card.setNumber(request.getNumber());
        card.setOwner(request.getOwner());

        cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(User user, UUID cardId) {
        Card card = getCardAndValidateOwnership(cardId, user);

        // Cannot delete card with positive balance
        if (card.getBalance() > 0) {
            throw new BusinessException("Cannot delete card with positive balance. Balance: " + card.getBalance());
        }

        cardRepository.delete(card);
    }

    @Transactional
    public void blockCard(User user, UUID cardId) {
        Card card = getCardAndValidateOwnership(cardId, user);

        if (card.getStatus() == CardStatusEnum.BLOCKED) {
            throw new BusinessException("Card is already blocked");
        }

        card.setStatus(CardStatusEnum.BLOCKED);
        cardRepository.save(card);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private Card getCardAndValidateOwnership(UUID cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to access this card");
        }

        return card;
    }

    private static CardResponse toDto(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .number(CardMaskerHelper.mask(card.getNumber()))
                .balance(card.getBalance())
                .owner(card.getOwner())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .build();
    }
}