package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CouponAdminFacade {

    private final CouponTemplateService couponTemplateService;
    private final IssuedCouponService issuedCouponService;

    @Transactional
    public CouponTemplateInfo createTemplate(String name, CouponType type, Long value,
                                             Long minOrderAmount, ZonedDateTime expiredAt) {
        CouponTemplate template = couponTemplateService.create(
            new CouponTemplate(name, type, value, minOrderAmount, expiredAt));
        return CouponTemplateInfo.from(template);
    }

    @Transactional(readOnly = true)
    public CouponTemplateInfo getTemplate(Long couponTemplateId) {
        return CouponTemplateInfo.from(couponTemplateService.getById(couponTemplateId));
    }

    @Transactional(readOnly = true)
    public Page<CouponTemplateInfo> getTemplates(Pageable pageable) {
        return couponTemplateService.getAll(pageable).map(CouponTemplateInfo::from);
    }

    @Transactional
    public CouponTemplateInfo updateTemplate(Long couponTemplateId, String name, Long value,
                                             Long minOrderAmount, ZonedDateTime expiredAt) {
        CouponTemplate template = couponTemplateService.update(
            couponTemplateId, name, value, minOrderAmount, expiredAt);
        return CouponTemplateInfo.from(template);
    }

    @Transactional
    public void deleteTemplate(Long couponTemplateId) {
        couponTemplateService.delete(couponTemplateId);
    }

    @Transactional(readOnly = true)
    public Page<IssuedCouponInfo> getIssues(Long couponTemplateId, Pageable pageable) {
        CouponTemplate template = couponTemplateService.getById(couponTemplateId);
        return issuedCouponService.getByCouponTemplateId(couponTemplateId, pageable)
            .map(issued -> IssuedCouponInfo.from(issued, template));
    }
}
