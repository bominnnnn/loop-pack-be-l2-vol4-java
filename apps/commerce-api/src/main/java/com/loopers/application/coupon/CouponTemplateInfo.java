package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;
import com.loopers.domain.coupon.CouponType;

import java.time.ZonedDateTime;

public record CouponTemplateInfo(
    Long couponTemplateId,
    String name,
    CouponType type,
    Long value,
    Long minOrderAmount,
    ZonedDateTime expiredAt
) {
    public static CouponTemplateInfo from(CouponTemplate template) {
        return new CouponTemplateInfo(
            template.getId(),
            template.getName(),
            template.getType(),
            template.getValue(),
            template.getMinOrderAmount(),
            template.getExpiredAt()
        );
    }
}
