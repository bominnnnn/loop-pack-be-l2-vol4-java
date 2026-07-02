package com.loopers.support;

import com.loopers.application.payment.PaymentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * PENDING 상태 결제 주기적 동기화.
 * PG 콜백이 유실된 경우 폴링으로 복구한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentSyncScheduler {

    private final PaymentFacade paymentFacade;

    @Scheduled(fixedDelay = 60_000)
    public void syncPendingPayments() {
        log.info("[PaymentSyncScheduler] PENDING 결제 동기화 실행");
        paymentFacade.syncAllPendingPayments();
    }
}
