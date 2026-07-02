package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponAdminFacade;
import com.loopers.application.coupon.CouponTemplateInfo;
import com.loopers.application.coupon.IssuedCouponInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/coupons")
public class CouponAdminController {

    private final CouponAdminFacade couponAdminFacade;

    @GetMapping
    public ApiResponse<Page<CouponAdminDto.CouponTemplateResponse>> getTemplates(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<CouponTemplateInfo> result = couponAdminFacade.getTemplates(PageRequest.of(page, size));
        return ApiResponse.success(result.map(CouponAdminDto.CouponTemplateResponse::from));
    }

    @GetMapping("/{couponId}")
    public ApiResponse<CouponAdminDto.CouponTemplateResponse> getTemplate(@PathVariable Long couponId) {
        CouponTemplateInfo info = couponAdminFacade.getTemplate(couponId);
        return ApiResponse.success(CouponAdminDto.CouponTemplateResponse.from(info));
    }

    @PostMapping
    public ApiResponse<CouponAdminDto.CouponTemplateResponse> createTemplate(
        @RequestBody CouponAdminDto.CreateTemplateRequest request
    ) {
        CouponTemplateInfo info = couponAdminFacade.createTemplate(
            request.name(), request.type(), request.value(), request.minOrderAmount(), request.expiredAt());
        return ApiResponse.success(CouponAdminDto.CouponTemplateResponse.from(info));
    }

    @PutMapping("/{couponId}")
    public ApiResponse<CouponAdminDto.CouponTemplateResponse> updateTemplate(
        @PathVariable Long couponId,
        @RequestBody CouponAdminDto.UpdateTemplateRequest request
    ) {
        CouponTemplateInfo info = couponAdminFacade.updateTemplate(
            couponId, request.name(), request.value(), request.minOrderAmount(), request.expiredAt());
        return ApiResponse.success(CouponAdminDto.CouponTemplateResponse.from(info));
    }

    @DeleteMapping("/{couponId}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long couponId) {
        couponAdminFacade.deleteTemplate(couponId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{couponId}/issues")
    public ApiResponse<Page<CouponAdminDto.IssuedCouponResponse>> getIssues(
        @PathVariable Long couponId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<IssuedCouponInfo> result = couponAdminFacade.getIssues(couponId, PageRequest.of(page, size));
        return ApiResponse.success(result.map(CouponAdminDto.IssuedCouponResponse::from));
    }
}
