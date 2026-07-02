package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.IssuedCoupon;
import com.loopers.domain.coupon.IssuedCouponStatus;

import java.time.ZonedDateTime;

public record IssuedCouponInfo(
    Long issuedCouponId,
    Long userId,
    Long couponTemplateId,
    String couponName,
    CouponType type,
    Long value,
    Long minOrderAmount,
    IssuedCouponStatus status,
    ZonedDateTime expiredAt
) {
    public static IssuedCouponInfo from(IssuedCoupon issuedCoupon, CouponTemplate template) {
        return new IssuedCouponInfo(
            issuedCoupon.getId(),
            issuedCoupon.getUserId(),
            template.getId(),
            template.getName(),
            template.getType(),
            template.getValue(),
            template.getMinOrderAmount(),
            issuedCoupon.resolvedStatus(template),
            template.getExpiredAt()
        );
    }
}
