package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_status", columnList = "status")
    }
)
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pg_transaction_id")
    private String pgTransactionId;

    @Column(name = "card_type", nullable = false)
    private String cardType;

    @Column(name = "card_no", nullable = false)
    private String cardNo;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    protected Payment() {}

    public Payment(Long orderId, Long userId, String cardType, String cardNo, Long amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public String getPgTransactionId() { return pgTransactionId; }
    public String getCardType() { return cardType; }
    public String getCardNo() { return cardNo; }
    public Long getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }

    public void approve(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "PENDING 상태의 결제만 승인할 수 있습니다.");
        }
        this.pgTransactionId = pgTransactionId;
        this.status = PaymentStatus.APPROVED;
    }

    public void fail(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "PENDING 상태의 결제만 실패 처리할 수 있습니다.");
        }
        this.pgTransactionId = pgTransactionId;
        this.status = PaymentStatus.FAILED;
    }
}
