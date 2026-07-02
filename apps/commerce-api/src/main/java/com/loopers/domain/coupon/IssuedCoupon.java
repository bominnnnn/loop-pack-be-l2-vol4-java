package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(
    name = "issued_coupons",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coupon_template_id"}),
    indexes = {
        @Index(name = "idx_issued_coupons_user", columnList = "user_id"),
        @Index(name = "idx_issued_coupons_template", columnList = "coupon_template_id")
    }
)
public class IssuedCoupon extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_template_id", nullable = false)
    private Long couponTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuedCouponStatus status;

    protected IssuedCoupon() {}

    public IssuedCoupon(Long userId, Long couponTemplateId) {
        this.userId = userId;
        this.couponTemplateId = couponTemplateId;
        this.status = IssuedCouponStatus.AVAILABLE;
    }

    public void use(CouponTemplate template, long orderTotalAmount) {
        if (this.status != IssuedCouponStatus.AVAILABLE) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용 불가능한 쿠폰입니다.");
        }
        if (template.isExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰입니다.");
        }
        Long minOrderAmount = template.getMinOrderAmount();
        if (minOrderAmount != null && orderTotalAmount < minOrderAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST,
                "최소 주문 금액(" + minOrderAmount + "원) 이상이어야 쿠폰을 사용할 수 있습니다.");
        }
        this.status = IssuedCouponStatus.USED;
    }

    public void restore() {
        if (this.status != IssuedCouponStatus.USED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용된 쿠폰만 복원할 수 있습니다.");
        }
        this.status = IssuedCouponStatus.AVAILABLE;
    }

    public IssuedCouponStatus resolvedStatus(CouponTemplate template) {
        if (this.status == IssuedCouponStatus.USED) return IssuedCouponStatus.USED;
        if (template.isExpired()) return IssuedCouponStatus.EXPIRED;
        return IssuedCouponStatus.AVAILABLE;
    }

    public boolean belongsTo(Long userId) {
        return this.userId.equals(userId);
    }

    public Long getUserId() { return userId; }
    public Long getCouponTemplateId() { return couponTemplateId; }
    public IssuedCouponStatus getStatus() { return status; }
}
