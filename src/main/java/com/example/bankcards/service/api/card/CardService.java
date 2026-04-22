package com.example.bankcards.service.api.card;

import com.example.bankcards.dto.api.card.BalanceResponse;
import com.example.bankcards.dto.api.card.CardListRequest;
import com.example.bankcards.dto.api.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CardPanCodec;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.enums.CardBlockRequestStatusEnum;
import com.example.bankcards.util.enums.CardStatusEnum;
import com.example.bankcards.util.helper.CardHelper;
import com.example.bankcards.util.helper.CardMaskerHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final CardPanCodec cardPanCodec;

    @Transactional
    public PaginationResponse<CardResponse> getUserCards(User authUser, CardListRequest cardListRequest) {

        String rawFilter = cardListRequest.getNumber();
        String norm = rawFilter == null ? "" : cardPanCodec.normalize(rawFilter);
        boolean hasNorm = !norm.isEmpty();
        String hmac = hasNorm && norm.length() > 4 ? cardPanCodec.hmacHex(norm) : "";

        List<Card> cards = cardRepository.findByUserIdAndNumberAndPagination(
                authUser.getId(),
                hasNorm,
                norm,
                hmac,
                cardListRequest.getPagination().getOffset(),
                cardListRequest.getPagination().getLimit()
            );

        List<Card> updatedCards = new ArrayList<>();
        for (Card card : cards) {
            if (CardHelper.markExpiredIfNeeded(card)) {
                updatedCards.add(card);
            }
        }
        if (!updatedCards.isEmpty()) {
            cardRepository.saveAll(updatedCards);
        }

        List<CardResponse> cardList = cards.stream().map(this::toDto).toList();

        int total = cardRepository.countByUserId(authUser.getId());

        return new PaginationResponse<>(cardList, cardListRequest.getPagination(), total);
    }

    @Transactional
    public BalanceResponse getCardBalance(User user, UUID cardId) {
        Card card = getCardAndValidateOwnership(cardId, user);
        if (CardHelper.markExpiredIfNeeded(card)) {
            cardRepository.save(card);
        }

        return BalanceResponse.builder()
                .cardId(card.getId())
                .cardNumber(CardMaskerHelper.mask(cardPanCodec.readPlainPan(card)))
                .balance(card.getBalance())
                .status(card.getStatus().toString())
                .build();
    }

    @Transactional
    public void blockCard(User user, UUID cardId) {
        Card card = getCardAndValidateOwnership(cardId, user);
        if (CardHelper.markExpiredIfNeeded(card)) {
            cardRepository.save(card);
            throw new BadRequestException("Card is expired");
        }

        if (card.getStatus() == CardStatusEnum.BLOCKED) {
            throw new BadRequestException("Card is already blocked");
        }

        if (cardBlockRequestRepository.existsByCardIdAndUserIdAndStatus(
                card.getId(), user.getId(), CardBlockRequestStatusEnum.PENDING
        )) {
            throw new BadRequestException("Block request is already pending");
        }

        CardBlockRequest request = new CardBlockRequest();
        request.setCard(card);
        request.setUser(user);
        request.setStatus(CardBlockRequestStatusEnum.PENDING);
        cardBlockRequestRepository.save(request);
    }

    private Card getCardAndValidateOwnership(UUID cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to access this card");
        }

        return card;
    }

    private CardResponse toDto(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .number(CardMaskerHelper.mask(cardPanCodec.readPlainPan(card)))
                .balance(card.getBalance())
                .owner(card.getOwner())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .build();
    }
}
