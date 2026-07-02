package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponTemplateService {

    private final CouponTemplateRepository couponTemplateRepository;

    public CouponTemplate create(CouponTemplate couponTemplate) {
        return couponTemplateRepository.save(couponTemplate);
    }

    public CouponTemplate getById(Long id) {
        return couponTemplateRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰 템플릿입니다."));
    }

    public Page<CouponTemplate> getAll(Pageable pageable) {
        return couponTemplateRepository.findAll(pageable);
    }

    public CouponTemplate update(Long id, String name, Long value, Long minOrderAmount,
                                 java.time.ZonedDateTime expiredAt) {
        CouponTemplate template = getById(id);
        template.update(name, value, minOrderAmount, expiredAt);
        return couponTemplateRepository.save(template);
    }

    public void delete(Long id) {
        CouponTemplate template = getById(id);
        couponTemplateRepository.delete(template);
    }
}
