package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;

public record PaymentInfo(
    Long paymentId,
    Long orderId,
    Long userId,
    String pgTransactionId,
    String cardType,
    Long amount,
    PaymentStatus status
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
            payment.getId(),
            payment.getOrderId(),
            payment.getUserId(),
            payment.getPgTransactionId(),
            payment.getCardType(),
            payment.getAmount(),
            payment.getStatus()
        );
    }
}
