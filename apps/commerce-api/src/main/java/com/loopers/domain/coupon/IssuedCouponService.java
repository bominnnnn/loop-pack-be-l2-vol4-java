package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class IssuedCouponService {

    private final IssuedCouponRepository issuedCouponRepository;

    @Transactional
    public IssuedCoupon issue(Long userId, CouponTemplate template) {
        if (template.isExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰 템플릿입니다.");
        }
        if (issuedCouponRepository.existsByUserIdAndCouponTemplateId(userId, template.getId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 발급받은 쿠폰입니다.");
        }
        return issuedCouponRepository.save(new IssuedCoupon(userId, template.getId()));
    }

    @Transactional(readOnly = true)
    public IssuedCoupon getById(Long id) {
        return issuedCouponRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 발급 쿠폰입니다."));
    }

    @Transactional(readOnly = true)
    public List<IssuedCoupon> getByUserId(Long userId) {
        return issuedCouponRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<IssuedCoupon> getByCouponTemplateId(Long couponTemplateId, Pageable pageable) {
        return issuedCouponRepository.findAllByCouponTemplateId(couponTemplateId, pageable);
    }

    /**
     * 비관적 락으로 조회 후 사용 처리.
     * 동시에 같은 쿠폰으로 요청이 들어와도 한 트랜잭션씩 순서대로 처리된다.
     *
     * A, B 동시 진입 (status=AVAILABLE)
     * A: FOR UPDATE 락 획득 → use() → USED → 커밋 → 락 해제
     * B: 락 대기 → 획득 → status=USED → use() 예외 → 롤백
     */
    @Transactional
    public IssuedCoupon use(Long issuedCouponId, Long userId, CouponTemplate template, long orderTotalAmount) {
        IssuedCoupon issuedCoupon = issuedCouponRepository.findByIdWithLock(issuedCouponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 발급 쿠폰입니다."));
        if (!issuedCoupon.belongsTo(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 쿠폰만 사용할 수 있습니다.");
        }
        issuedCoupon.use(template, orderTotalAmount);
        return issuedCouponRepository.save(issuedCoupon);
    }

    @Transactional
    public void restore(Long issuedCouponId) {
        IssuedCoupon issuedCoupon = getById(issuedCouponId);
        issuedCoupon.restore();
        issuedCouponRepository.save(issuedCoupon);
    }
}
