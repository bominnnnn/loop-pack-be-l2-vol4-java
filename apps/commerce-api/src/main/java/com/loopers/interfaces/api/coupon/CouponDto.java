package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.IssuedCouponInfo;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.IssuedCouponStatus;

import java.time.ZonedDateTime;
import java.util.List;

public class CouponDto {

    public record IssuedCouponResponse(
        Long issuedCouponId,
        Long couponTemplateId,
        String couponName,
        CouponType type,
        Long value,
        Long minOrderAmount,
        IssuedCouponStatus status,
        ZonedDateTime expiredAt
    ) {
        public static IssuedCouponResponse from(IssuedCouponInfo info) {
            return new IssuedCouponResponse(
                info.issuedCouponId(),
                info.couponTemplateId(),
                info.couponName(),
                info.type(),
                info.value(),
                info.minOrderAmount(),
                info.status(),
                info.expiredAt()
            );
        }
    }

    public record MyCouponsResponse(List<IssuedCouponResponse> coupons) {
        public static MyCouponsResponse from(List<IssuedCouponInfo> infos) {
            return new MyCouponsResponse(infos.stream().map(IssuedCouponResponse::from).toList());
        }
    }
}
