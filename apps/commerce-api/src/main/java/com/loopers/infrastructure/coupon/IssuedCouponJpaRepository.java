package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.IssuedCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssuedCouponJpaRepository extends JpaRepository<IssuedCoupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ic FROM IssuedCoupon ic WHERE ic.id = :id")
    Optional<IssuedCoupon> findByIdWithLock(@Param("id") Long id);

    List<IssuedCoupon> findAllByUserId(Long userId);

    Page<IssuedCoupon> findAllByCouponTemplateId(Long couponTemplateId, Pageable pageable);

    boolean existsByUserIdAndCouponTemplateId(Long userId, Long couponTemplateId);
}
