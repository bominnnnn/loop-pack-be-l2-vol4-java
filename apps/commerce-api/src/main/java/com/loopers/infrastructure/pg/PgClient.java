package com.loopers.infrastructure.pg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgClient {

    private final RestTemplate pgRestTemplate;

    @Value("${pg.base-url}")
    private String pgBaseUrl;

    @Value("${pg.callback-url}")
    private String callbackUrl;

    public PgPaymentResponse requestPayment(Long orderId, Long amount, String cardType, String cardNo) {
        String url = pgBaseUrl + "/api/v1/payments";
        PgPaymentRequest request = new PgPaymentRequest(orderId, amount, cardType, cardNo, callbackUrl);

        log.info("[PG] 결제 요청 - orderId={}, amount={}", orderId, amount);
        PgPaymentResponse response = pgRestTemplate.postForObject(url, request, PgPaymentResponse.class);
        log.info("[PG] 결제 응답 - pgTransactionId={}, status={}", response.pgTransactionId(), response.status());
        return response;
    }

    public PgPaymentStatusResponse getPaymentStatus(Long orderId) {
        String url = pgBaseUrl + "/api/v1/payments?orderId=" + orderId;
        log.info("[PG] 결제 상태 조회 - orderId={}", orderId);
        return pgRestTemplate.getForObject(url, PgPaymentStatusResponse.class);
    }

    public record PgPaymentRequest(
        Long orderId,
        Long amount,
        String cardType,
        String cardNo,
        String callbackUrl
    ) {}

    public record PgPaymentResponse(
        String pgTransactionId,
        Long orderId,
        String status
    ) {}

    public record PgPaymentStatusResponse(
        String pgTransactionId,
        Long orderId,
        String status
    ) {}
}
