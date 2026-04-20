package com.example.bankcards.service;

import com.example.bankcards.dto.admin.AdminCardDto;
import com.example.bankcards.dto.admin.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.helper.CardMaskerHelper;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdminCardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public AdminCardService(CardRepository cards, UserRepository users) {
        this.cardRepository = cards;
        this.userRepository = users;
    }

    public PaginationResponse<AdminCardDto> list(Pageable pageable) {
        List<Card> cards = cardRepository.findAllWithPaginationAndRoles(pageable.getOffset(), pageable.getPageSize());
        List<AdminCardDto> cardDto = cards.stream().map(AdminCardService::toDto).toList();

        int total = (int)cardRepository.count();

        return new PaginationResponse<>(cardDto, pageable, total);
    }

    public AdminCardDto get(UUID cardId) {
        return cardRepository.findById(cardId).map(AdminCardService::toDto)
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    @Transactional
    public AdminCardDto create(CreateCardRequest req) {
        User user = userRepository.findById(req.userId()).orElseThrow(() -> new NotFoundException("User not found"));

        String normalizedNumber = req.number().replaceAll("\\s+", "");
        if (!normalizedNumber.matches("\\d{12,19}")) {
            throw new BadRequestException("Card number must contain 12-19 digits");
        }
        if (cardRepository.existsByNumber(normalizedNumber)) {
            throw new BadRequestException("Card number already exists");
        }
        if (req.expiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Expiry date must be in the future");
        }

        Card card = new Card();
        card.setUser(user);
        card.setNumber(normalizedNumber);
        card.setOwner(req.owner());
        card.setExpiryDate(req.expiryDate());
        card.setBalance(req.balance());
        card.setStatus(CardStatusEnum.ACTIVE);
        cardRepository.save(card);

        return toDto(card);
    }

    @Transactional
    public AdminCardDto block(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));
        card.setStatus(CardStatusEnum.BLOCKED);
        return toDto(card);
    }

    @Transactional
    public AdminCardDto activate(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));
        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatusEnum.DEADLINE_EXPIRED);
            throw new BadRequestException("Card is expired");
        }
        card.setStatus(CardStatusEnum.ACTIVE);
        return toDto(card);
    }

    @Transactional
    public void delete(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    private static AdminCardDto toDto(Card c) {
        return new AdminCardDto(
                c.getId(),
                CardMaskerHelper.mask(c.getNumber()),
                c.getOwner(),
                c.getExpiryDate(),
                c.getStatus(),
                c.getBalance(),
                c.getUser() == null ? null : c.getUser().getId(),
                c.getCreated(),
                c.getUpdated()
        );
    }
}

