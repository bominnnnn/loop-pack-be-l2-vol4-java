package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponTemplateInfo;
import com.loopers.application.coupon.IssuedCouponInfo;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.IssuedCouponStatus;

import java.time.ZonedDateTime;

public class CouponAdminDto {

    public record CreateTemplateRequest(
        String name,
        CouponType type,
        Long value,
        Long minOrderAmount,
        ZonedDateTime expiredAt
    ) {}

    public record UpdateTemplateRequest(
        String name,
        Long value,
        Long minOrderAmount,
        ZonedDateTime expiredAt
    ) {}

    public record CouponTemplateResponse(
        Long couponTemplateId,
        String name,
        CouponType type,
        Long value,
        Long minOrderAmount,
        ZonedDateTime expiredAt
    ) {
        public static CouponTemplateResponse from(CouponTemplateInfo info) {
            return new CouponTemplateResponse(
                info.couponTemplateId(),
                info.name(),
                info.type(),
                info.value(),
                info.minOrderAmount(),
                info.expiredAt()
            );
        }
    }

    public record IssuedCouponResponse(
        Long issuedCouponId,
        Long userId,
        Long couponTemplateId,
        IssuedCouponStatus status
    ) {
        public static IssuedCouponResponse from(IssuedCouponInfo info) {
            return new IssuedCouponResponse(
                info.issuedCouponId(),
                info.userId(),
                info.couponTemplateId(),
                info.status()
            );
        }
    }
}
