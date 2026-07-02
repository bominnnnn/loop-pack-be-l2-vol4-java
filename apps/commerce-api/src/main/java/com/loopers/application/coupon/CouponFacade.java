package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponTemplateService couponTemplateService;
    private final IssuedCouponService issuedCouponService;

    @Transactional
    public IssuedCouponInfo issueCoupon(Long couponTemplateId, Long userId) {
        CouponTemplate template = couponTemplateService.getById(couponTemplateId);
        IssuedCoupon issued = issuedCouponService.issue(userId, template);
        return IssuedCouponInfo.from(issued, template);
    }

    @Transactional(readOnly = true)
    public List<IssuedCouponInfo> getMyCoupons(Long userId) {
        List<IssuedCoupon> issuedCoupons = issuedCouponService.getByUserId(userId);
        if (issuedCoupons.isEmpty()) return List.of();

        Map<Long, CouponTemplate> templateMap = issuedCoupons.stream()
            .map(IssuedCoupon::getCouponTemplateId)
            .distinct()
            .map(couponTemplateService::getById)
            .collect(Collectors.toMap(CouponTemplate::getId, Function.identity()));

        return issuedCoupons.stream()
            .map(issued -> IssuedCouponInfo.from(issued, templateMap.get(issued.getCouponTemplateId())))
            .toList();
    }
}
