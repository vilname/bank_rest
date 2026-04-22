package com.example.bankcards.service.admin.card;

import com.example.bankcards.dto.admin.AdminCardResponse;
import com.example.bankcards.dto.admin.AdminCardBlockRequestResponse;
import com.example.bankcards.dto.admin.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CardPanCodec;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.enums.CardBlockRequestStatusEnum;
import com.example.bankcards.util.enums.CardStatusEnum;
import com.example.bankcards.util.helper.CardMaskerHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final CardRepository cardRepository;
    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final UserRepository userRepository;
    private final CardPanCodec cardPanCodec;

    public PaginationResponse<AdminCardResponse> list(PaginationRequest pagination) {
        List<Card> cards = cardRepository.findAllByPagination(pagination.getOffset(), pagination.getLimit());
        List<AdminCardResponse> cardDto = cards.stream().map(this::toDto).toList();

        int total = (int) cardRepository.count();

        return new PaginationResponse<>(cardDto, pagination, total);
    }

    public AdminCardResponse get(UUID cardId) {
        return cardRepository.findById(cardId).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    @Transactional
    public void create(CreateCardRequest req) {
        User user = userRepository.findById(req.userId()).orElseThrow(() -> new NotFoundException("User not found"));

        String normalizedNumber = cardPanCodec.normalize(req.number());
        if (!normalizedNumber.matches("\\d{12,19}")) {
            throw new BadRequestException("Card number must contain 12-19 digits");
        }
        if (cardPanCodec.plainPanExists(normalizedNumber)) {
            throw new BadRequestException("Card number already exists");
        }
        if (req.expiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Expiry date must be in the future");
        }

        Card card = new Card();
        card.setUser(user);
        cardPanCodec.writeEncryptedPan(card, normalizedNumber);
        card.setOwner(req.owner());
        card.setExpiryDate(req.expiryDate());
        card.setBalance(req.balance());
        card.setStatus(CardStatusEnum.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional
    public void block(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));
        card.setStatus(CardStatusEnum.BLOCKED);

        cardRepository.save(card);
    }

    @Transactional
    public void activate(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));
        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatusEnum.DEADLINE_EXPIRED);
            throw new BadRequestException("Card is expired");
        }
        card.setStatus(CardStatusEnum.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional
    public void delete(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    public PaginationResponse<AdminCardBlockRequestResponse> listPendingBlockRequests(PaginationRequest pagination) {
        List<CardBlockRequest> requests = cardBlockRequestRepository.findByStatusWithPagination(
                CardBlockRequestStatusEnum.PENDING.name(),
                pagination.getOffset(),
                pagination.getLimit()
        );
        List<AdminCardBlockRequestResponse> dto = requests.stream().map(this::toBlockRequestDto).toList();
        int total = cardBlockRequestRepository.countByStatus(CardBlockRequestStatusEnum.PENDING);
        return new PaginationResponse<>(dto, pagination, total);
    }

    @Transactional
    public void approveBlockRequest(UUID requestId) {
        CardBlockRequest request = getPendingRequest(requestId);
        Card card = request.getCard();
        card.setStatus(CardStatusEnum.BLOCKED);
        request.setStatus(CardBlockRequestStatusEnum.APPROVED);
        cardRepository.save(card);
        cardBlockRequestRepository.save(request);
    }

    @Transactional
    public void rejectBlockRequest(UUID requestId) {
        CardBlockRequest request = getPendingRequest(requestId);
        request.setStatus(CardBlockRequestStatusEnum.REJECTED);
        cardBlockRequestRepository.save(request);
    }

    private CardBlockRequest getPendingRequest(UUID requestId) {
        CardBlockRequest request = cardBlockRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Block request not found"));
        if (request.getStatus() != CardBlockRequestStatusEnum.PENDING) {
            throw new BadRequestException("Block request is already processed");
        }
        return request;
    }

    private AdminCardResponse toDto(Card c) {
        return new AdminCardResponse(
                c.getId(),
                CardMaskerHelper.mask(cardPanCodec.readPlainPan(c)),
                c.getOwner(),
                c.getExpiryDate(),
                c.getStatus(),
                c.getBalance(),
                c.getUser() == null ? null : c.getUser().getId(),
                c.getCreated(),
                c.getUpdated()
        );
    }

    private AdminCardBlockRequestResponse toBlockRequestDto(CardBlockRequest request) {
        return new AdminCardBlockRequestResponse(
                request.getId(),
                request.getCard().getId(),
                CardMaskerHelper.mask(cardPanCodec.readPlainPan(request.getCard())),
                request.getUser().getId(),
                request.getStatus(),
                request.getCreated()
        );
    }
}
