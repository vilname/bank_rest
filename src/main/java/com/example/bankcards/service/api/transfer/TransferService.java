package com.example.bankcards.service.api.transfer;

import com.example.bankcards.dto.api.transfer.TransferRequest;
import com.example.bankcards.dto.api.transfer.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.CardPanCodec;
import com.example.bankcards.util.helper.CardMaskerHelper;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.enums.CardStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final CardPanCodec cardPanCodec;

    @Transactional
    public TransferResponse makeTransfer(User user, TransferRequest request) {

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new BusinessException("Source card not found"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new BusinessException("Destination card not found"));


        if (!fromCard.getUser().getId().equals(user.getId()) ||
                !toCard.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Both cards must belong to you");
        }

        // если перевод на карту из которой средства снимаются
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BusinessException("Cannot transfer to the same card");
        }

        // карта с которой списываются не активна
        if (fromCard.getStatus() != CardStatusEnum.ACTIVE) {
            throw new BusinessException("Source card is not active");
        }

        // карта куда зачисляются средства не активна
        if (toCard.getStatus() != CardStatusEnum.ACTIVE) {
            throw new BusinessException("Destination card is not active");
        }

        // у карты списания не достаточно средств
        if (fromCard.getBalance() < request.getAmount()) {
            throw new BusinessException(
                    "Insufficient balance. Available: " + fromCard.getBalance() +
                            ", Requested: " + request.getAmount());
        }

        fromCard.setBalance(fromCard.getBalance() - request.getAmount());
        toCard.setBalance(toCard.getBalance() + request.getAmount());

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = new Transfer();
        transfer.setBalance(request.getAmount());
        transfer.setCardFrom(fromCard);
        transfer.setCardTo(toCard);
        transfer.setUser(user);

        Transfer savedTransfer = transferRepository.save(transfer);

        return mapToResponse(savedTransfer, fromCard, toCard);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<TransferResponse> getUserTransfers(User user, PaginationRequest pagination) {
        List<Transfer> transfers
                = transferRepository.findUserTransfersWithPagination(user.getId(), pagination.getOffset(), pagination.getLimit());

        int total = (int) cardRepository.count();

        List<TransferResponse> transferList = transfers.stream()
                .map(transfer -> mapToResponse(
                        transfer, transfer.getCardFrom(), transfer.getCardTo())
                )
                .toList();


        return new PaginationResponse<>(transferList, pagination, total);
    }

    private TransferResponse mapToResponse(Transfer transfer, Card fromCard, Card toCard) {
        TransferResponse.CardResponse fromCardInfo = TransferResponse.CardResponse.builder()
                .id(fromCard.getId())
                .balance(fromCard.getBalance())
                .number(CardMaskerHelper.mask(cardPanCodec.readPlainPan(fromCard)))
                .build();

        TransferResponse.CardResponse toCardInfo = TransferResponse.CardResponse.builder()
                .id(toCard.getId())
                .balance(toCard.getBalance())
                .number(CardMaskerHelper.mask(cardPanCodec.readPlainPan(toCard)))
                .build();

        return TransferResponse.builder()
                .id(transfer.getId())
                .balance(transfer.getBalance())
                .created(transfer.getCreated())
                .fromCard(fromCardInfo)
                .fromCardNumber(CardMaskerHelper.mask(cardPanCodec.readPlainPan(fromCard)))
                .toCard(toCardInfo)
                .toCardNumber(CardMaskerHelper.mask(cardPanCodec.readPlainPan(toCard)))
                .build();
    }
}
