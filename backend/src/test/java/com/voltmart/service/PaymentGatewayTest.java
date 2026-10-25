package com.voltmart.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentGatewayTest {
    private final PaymentGateway gateway = new PaymentGateway();

    @Test
    void capturesValidVisa() {
        var result = gateway.charge("4242424242424242", "12/29", "123", new BigDecimal("10.00"));
        assertTrue(result.success());
        assertTrue(result.verified());
        assertEquals("4242", result.last4());
        assertEquals("VISA", result.brand());
        assertNotNull(result.transactionRef());
    }

    @Test
    void declinesTestCardEnding0000() {
        var result = gateway.charge("4111111111110000", "12/29", "123", new BigDecimal("10.00"));
        assertFalse(result.success());
        assertEquals("Card declined", result.failureReason());
    }
}
