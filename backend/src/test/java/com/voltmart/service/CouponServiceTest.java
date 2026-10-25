package com.voltmart.service;

import com.voltmart.domain.entity.Coupon;
import com.voltmart.exception.ApiException;
import com.voltmart.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouponServiceTest {
    private CouponRepository couponRepository;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        couponService = new CouponService(couponRepository);
    }

    @Test
    void percentDiscountRoundsHalfUp() {
        Coupon coupon = active("WELCOME10", "PERCENT", new BigDecimal("10"), BigDecimal.ZERO);
        when(couponRepository.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(coupon));
        BigDecimal discount = couponService.calculate(coupon, new BigDecimal("199.99"));
        assertEquals(new BigDecimal("20.00"), discount);
    }

    @Test
    void rejectsBelowMinimum() {
        Coupon coupon = active("SAVE15", "FIXED", new BigDecimal("15"), new BigDecimal("100"));
        when(couponRepository.findByCodeIgnoreCase("SAVE15")).thenReturn(Optional.of(coupon));
        ApiException ex = assertThrows(ApiException.class,
                () -> couponService.requireValid("SAVE15", new BigDecimal("40")));
        assertTrue(ex.getMessage().contains("minimum"));
    }

    @Test
    void previewInvalidCode() {
        when(couponRepository.findByCodeIgnoreCase("NOPE")).thenReturn(Optional.empty());
        var preview = couponService.preview("NOPE", new BigDecimal("80"));
        assertFalse(preview.valid());
    }

    private Coupon active(String code, String type, BigDecimal amount, BigDecimal min) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountType(type);
        coupon.setAmount(amount);
        coupon.setMinOrder(min);
        coupon.setActive(true);
        coupon.setStartsAt(Instant.now().minusSeconds(60));
        coupon.setEndsAt(Instant.now().plusSeconds(3600));
        return coupon;
    }
}
