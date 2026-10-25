package com.voltmart.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentGateway {

    public record ChargeResult(boolean success, boolean verified, String transactionRef,
                               String last4, String brand, String failureReason) {}

    public ChargeResult charge(String cardNumber, String expiry, String cvc, BigDecimal amount) {
        if (cardNumber == null || cardNumber.length() < 13) {
            return new ChargeResult(false, false, null, null, null, "Invalid card number");
        }
        if (cardNumber.endsWith("0000")) {
            return new ChargeResult(false, false, null, last4(cardNumber), brand(cardNumber), "Card declined");
        }
        if (cvc == null || cvc.length() < 3) {
            return new ChargeResult(false, false, null, last4(cardNumber), brand(cardNumber), "Invalid CVC");
        }
        String ref = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return new ChargeResult(true, true, ref, last4(cardNumber), brand(cardNumber), null);
    }

    public String refund(String originalRef, BigDecimal amount) {
        return "rf_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String last4(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private String brand(String cardNumber) {
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        return "CARD";
    }
}
