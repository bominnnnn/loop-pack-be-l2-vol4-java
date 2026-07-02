package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentStatus;

public class PaymentDto {

    public record PaymentRequest(
        Long orderId,
        String cardType,
        String cardNo
    ) {}

    public record CallbackRequest(
        String pgTransactionId,
        Long orderId,
        String status
    ) {}

    public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String pgTransactionId,
        String cardType,
        Long amount,
        PaymentStatus status
    ) {
        public static PaymentResponse from(PaymentInfo info) {
            return new PaymentResponse(
                info.paymentId(),
                info.orderId(),
                info.pgTransactionId(),
                info.cardType(),
                info.amount(),
                info.status()
            );
        }
    }
}
