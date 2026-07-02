package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ApiResponse<PaymentDto.PaymentResponse> requestPayment(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PaymentDto.PaymentRequest request
    ) {
        PaymentInfo info = paymentFacade.requestPayment(
            principal.getId(), request.orderId(), request.cardType(), request.cardNo()
        );
        return ApiResponse.success(PaymentDto.PaymentResponse.from(info));
    }

    @PostMapping("/callback")
    public ApiResponse<Void> handleCallback(
        @RequestBody PaymentDto.CallbackRequest request
    ) {
        paymentFacade.handleCallback(request.orderId(), request.pgTransactionId(), request.status());
        return ApiResponse.success(null);
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<PaymentDto.PaymentResponse> getPayment(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long orderId
    ) {
        PaymentInfo info = paymentFacade.getPayment(orderId, principal.getId());
        return ApiResponse.success(PaymentDto.PaymentResponse.from(info));
    }

    @PostMapping("/orders/{orderId}/sync")
    public ApiResponse<PaymentDto.PaymentResponse> syncPayment(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long orderId
    ) {
        PaymentInfo info = paymentFacade.syncPaymentStatus(orderId, principal.getId());
        return ApiResponse.success(PaymentDto.PaymentResponse.from(info));
    }
}
