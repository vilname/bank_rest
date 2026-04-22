package com.example.bankcards.util.helper;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.enums.CardStatusEnum;

import java.time.LocalDate;

public class CardHelper {

    public static boolean markExpiredIfNeeded(Card card) {
        if (card.getExpiryDate() != null
                && card.getExpiryDate().isBefore(LocalDate.now())
                && card.getStatus() != CardStatusEnum.DEADLINE_EXPIRED) {
            card.setStatus(CardStatusEnum.DEADLINE_EXPIRED);
            return true;
        }
        return false;
    }
}
