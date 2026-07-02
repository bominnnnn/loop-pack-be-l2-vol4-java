package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPending(Long orderId, Long userId, String cardType, String cardNo, Long amount) {
        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            throw new CoreException(ErrorType.CONFLICT, "이미 결제가 요청된 주문입니다.");
        });
        return paymentRepository.save(new Payment(orderId, userId, cardType, cardNo, amount));
    }

    @Transactional
    public Payment approve(Long orderId, String pgTransactionId) {
        Payment payment = getByOrderId(orderId);
        payment.approve(pgTransactionId);
        return payment;
    }

    @Transactional
    public Payment fail(Long orderId, String pgTransactionId) {
        Payment payment = getByOrderId(orderId);
        payment.fail(pgTransactionId);
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문의 결제 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public Payment findOrCreatePending(Long orderId, Long userId, String cardType, String cardNo, Long amount) {
        return paymentRepository.findByOrderId(orderId)
            .orElseGet(() -> paymentRepository.save(new Payment(orderId, userId, cardType, cardNo, amount)));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPendingPayments() {
        return paymentRepository.findAllByStatus(PaymentStatus.PENDING);
    }
}
