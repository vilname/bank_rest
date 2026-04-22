package com.example.bankcards.security;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * AES-256-GCM encryption for stored PAN + HMAC fingerprint for uniqueness / exact lookup
 * and last-four for substring search in SQL.
 */
@Component
public class CardPanCodec {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CardRepository cardRepository;
    private final SecretKey aesKey;
    private final SecretKey hmacKey;

    public CardPanCodec(
            CardRepository cardRepository,
            @Value("${app.pan.secret}") String secretHex
    ) {
        this.cardRepository = cardRepository;
        byte[] master = HexFormat.of().parseHex(secretHex.replaceAll("\\s+", ""));
        if (master.length < 32) {
            throw new IllegalStateException("app.pan.secret must decode to at least 32 bytes (64 hex chars)");
        }
        byte[] digest = sha512(master);
        this.aesKey = new SecretKeySpec(Arrays.copyOfRange(digest, 0, 32), "AES");
        this.hmacKey = new SecretKeySpec(Arrays.copyOfRange(digest, 32, 64), "HmacSHA256");
    }

    private static byte[] sha512(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(input);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D+", "");
    }

    public String hmacHex(String normalizedDigits) {
        Objects.requireNonNull(normalizedDigits, "normalizedDigits");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            byte[] out = mac.doFinal(normalizedDigits.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failed", e);
        }
    }

    public String lastFour(String normalizedDigits) {
        if (normalizedDigits == null || normalizedDigits.isEmpty()) {
            return "";
        }
        if (normalizedDigits.length() <= 4) {
            return normalizedDigits;
        }
        return normalizedDigits.substring(normalizedDigits.length() - 4);
    }

    public String encryptToBase64(String normalizedDigits) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(normalizedDigits.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ct.length);
            buf.put(iv);
            buf.put(ct);
            return java.util.Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new IllegalStateException("PAN encryption failed", e);
        }
    }

    public String decryptFromBase64(String base64Cipher) {
        if (base64Cipher == null || base64Cipher.isBlank()) {
            return null;
        }
        try {
            byte[] all = java.util.Base64.getDecoder().decode(base64Cipher);
            ByteBuffer buf = ByteBuffer.wrap(all);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] ct = new byte[buf.remaining()];
            buf.get(ct);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(ct);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("PAN decryption failed", e);
        }
    }

    /**
     * Persist encrypted PAN and derived search fields.
     */
    public void writeEncryptedPan(Card card, String rawPan) {
        String normalized = normalize(rawPan);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Card number is empty");
        }
        card.setPanCipher(encryptToBase64(normalized));
        card.setPanHmac(hmacHex(normalized));
        card.setPanLastFour(lastFour(normalized));
    }

    public String readPlainPan(Card card) {
        if (card.getPanCipher() == null || card.getPanCipher().isBlank()) {
            throw new IllegalStateException("Card has no encrypted PAN");
        }
        return decryptFromBase64(card.getPanCipher());
    }

    public boolean plainPanExists(String rawOrNormalizedPan) {
        String n = normalize(rawOrNormalizedPan);
        if (n.isEmpty()) {
            return false;
        }
        return cardRepository.existsByPanHmac(hmacHex(n));
    }
}
