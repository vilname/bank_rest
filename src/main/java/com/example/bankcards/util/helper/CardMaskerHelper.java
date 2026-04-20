package com.example.bankcards.util.helper;

public final class CardMaskerHelper {
    private CardMaskerHelper() {}

    public static String mask(String number) {
        if (number == null) return null;
        String digits = number.replaceAll("\\s+", "");
        if (digits.length() <= 4) return "****";
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }
}

