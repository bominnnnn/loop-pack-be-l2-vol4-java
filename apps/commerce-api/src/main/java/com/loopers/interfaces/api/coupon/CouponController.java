package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.IssuedCouponInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class CouponController {

    private final CouponFacade couponFacade;

    @PostMapping("/coupons/{couponTemplateId}/issue")
    public ApiResponse<CouponDto.IssuedCouponResponse> issueCoupon(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long couponTemplateId
    ) {
        IssuedCouponInfo info = couponFacade.issueCoupon(couponTemplateId, principal.getId());
        return ApiResponse.success(CouponDto.IssuedCouponResponse.from(info));
    }

    @GetMapping("/users/me/coupons")
    public ApiResponse<CouponDto.MyCouponsResponse> getMyCoupons(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<IssuedCouponInfo> infos = couponFacade.getMyCoupons(principal.getId());
        return ApiResponse.success(CouponDto.MyCouponsResponse.from(infos));
    }
}
