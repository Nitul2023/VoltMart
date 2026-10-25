package com.voltmart.service;

import com.voltmart.domain.entity.Coupon;
import com.voltmart.dto.CommerceDtos.CouponPreview;
import com.voltmart.exception.ApiException;
import com.voltmart.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponPreview preview(String code, BigDecimal subtotal) {
        try {
            Coupon coupon = requireValid(code, subtotal);
            return new CouponPreview(coupon.getCode(), coupon.getDescription(),
                    calculate(coupon, subtotal), true, "Coupon applied");
        } catch (ApiException ex) {
            return new CouponPreview(code, null, BigDecimal.ZERO, false, ex.getMessage());
        }
    }

    public Coupon requireValid(String code, BigDecimal subtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> ApiException.badRequest("Coupon not found"));
        Instant now = Instant.now();
        if (!coupon.isActive()) {
            throw ApiException.badRequest("Coupon is inactive");
        }
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw ApiException.badRequest("Coupon is not yet active");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw ApiException.badRequest("Coupon has expired");
        }
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw ApiException.badRequest("Coupon usage limit reached");
        }
        if (subtotal.compareTo(coupon.getMinOrder()) < 0) {
            throw ApiException.badRequest("Order does not meet coupon minimum");
        }
        return coupon;
    }

    public BigDecimal calculate(Coupon coupon, BigDecimal subtotal) {
        if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
            return subtotal.multiply(coupon.getAmount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return coupon.getAmount().min(subtotal);
    }

    public void increment(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
}
