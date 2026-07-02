package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final PgClient pgClient;

    /**
     * 결제 요청 (동기 흐름)
     *
     * 1. 주문 검증 (userId 일치, 주문 상태 확인)
     * 2. PENDING 결제 생성
     * 3. PG 결제 요청 (CircuitBreaker + Timeout 적용)
     * 4. PG 응답으로 결제 상태 업데이트 (APPROVED/FAILED)
     *
     * PG 장애 시 Fallback: PENDING 상태로 반환 → 콜백/폴링으로 나중에 동기화
     */
    @Transactional
    @CircuitBreaker(name = "pg", fallbackMethod = "requestPaymentFallback")
    public PaymentInfo requestPayment(Long userId, Long orderId, String cardType, String cardNo) {
        Order order = orderService.getOrderForUser(orderId, userId);
        Payment payment = paymentService.createPending(orderId, userId, cardType, cardNo, order.getTotalPrice());

        PgClient.PgPaymentResponse pgResponse = pgClient.requestPayment(
            orderId, order.getTotalPrice(), cardType, cardNo
        );

        if ("APPROVED".equals(pgResponse.status())) {
            paymentService.approve(orderId, pgResponse.pgTransactionId());
        } else if ("FAILED".equals(pgResponse.status())) {
            paymentService.fail(orderId, pgResponse.pgTransactionId());
        }

        return PaymentInfo.from(paymentService.getByOrderId(orderId));
    }

    /**
     * CircuitBreaker Fallback
     * PG 장애(Timeout, 5xx, 연결 불가) 시 호출됨.
     * PENDING 상태 결제를 반환하여 비즈니스 흐름을 유지한다.
     * 이후 콜백 수신 또는 폴링 스케줄러로 상태를 복구한다.
     */
    @Transactional
    public PaymentInfo requestPaymentFallback(Long userId, Long orderId, String cardType, String cardNo, Throwable t) {
        log.warn("[PG Fallback] PG 연동 실패 - orderId={}, cause={}", orderId, t.getMessage());

        Payment payment = paymentService.findOrCreatePending(orderId, userId, cardType, cardNo,
            orderService.getOrderForUser(orderId, userId).getTotalPrice());

        return PaymentInfo.from(payment);
    }

    /**
     * PG 콜백 수신 처리
     * PG → POST /api/v1/payments/callback 으로 비동기 결과 수신
     */
    @Transactional
    public void handleCallback(Long orderId, String pgTransactionId, String pgStatus) {
        Payment payment = paymentService.getByOrderId(orderId);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("[PG Callback] 이미 처리된 결제 - orderId={}, status={}", orderId, payment.getStatus());
            return;
        }

        if ("APPROVED".equals(pgStatus)) {
            paymentService.approve(orderId, pgTransactionId);
            log.info("[PG Callback] 결제 승인 - orderId={}, pgTxId={}", orderId, pgTransactionId);
        } else {
            paymentService.fail(orderId, pgTransactionId);
            log.warn("[PG Callback] 결제 실패 - orderId={}, pgTxId={}", orderId, pgTransactionId);
        }
    }

    /**
     * 결제 상태 조회 (PG 폴링으로 동기화)
     * 콜백이 오지 않은 PENDING 결제를 수동 동기화할 때 사용
     */
    @Transactional
    public PaymentInfo syncPaymentStatus(Long orderId, Long userId) {
        Payment payment = paymentService.getByOrderId(orderId);
        if (!payment.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 결제만 조회할 수 있습니다.");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return PaymentInfo.from(payment);
        }

        PgClient.PgPaymentStatusResponse status = pgClient.getPaymentStatus(orderId);
        if (status != null && "APPROVED".equals(status.status())) {
            paymentService.approve(orderId, status.pgTransactionId());
        } else if (status != null && "FAILED".equals(status.status())) {
            paymentService.fail(orderId, status.pgTransactionId());
        }

        return PaymentInfo.from(paymentService.getByOrderId(orderId));
    }

    @Transactional(readOnly = true)
    public PaymentInfo getPayment(Long orderId, Long userId) {
        Payment payment = paymentService.getByOrderId(orderId);
        if (!payment.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 결제만 조회할 수 있습니다.");
        }
        return PaymentInfo.from(payment);
    }

    /**
     * PENDING 결제 일괄 동기화 (스케줄러에서 호출)
     */
    @Transactional
    public void syncAllPendingPayments() {
        List<Payment> pendings = paymentService.getPendingPayments();
        log.info("[PG Sync] PENDING 결제 동기화 시작 - {}건", pendings.size());

        for (Payment payment : pendings) {
            try {
                PgClient.PgPaymentStatusResponse status = pgClient.getPaymentStatus(payment.getOrderId());
                if (status == null) continue;

                if ("APPROVED".equals(status.status())) {
                    paymentService.approve(payment.getOrderId(), status.pgTransactionId());
                    log.info("[PG Sync] 승인 - orderId={}", payment.getOrderId());
                } else if ("FAILED".equals(status.status())) {
                    paymentService.fail(payment.getOrderId(), status.pgTransactionId());
                    log.warn("[PG Sync] 실패 - orderId={}", payment.getOrderId());
                }
            } catch (Exception e) {
                log.error("[PG Sync] 동기화 오류 - orderId={}, error={}", payment.getOrderId(), e.getMessage());
            }
        }
    }
}
