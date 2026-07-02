package com.loopers.concurrency;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("재고 동시성 테스트")
class StockConcurrencyTest extends IntegrationTestBase {

    @Autowired private OrderFacade orderFacade;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductStockRepository productStockRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @DisplayName("동일 상품에 대해 동시에 여러 주문이 요청되어도, 재고가 정상적으로 차감되어야 한다.")
    @Test
    void concurrentOrders_shouldDecrementStockCorrectly() throws InterruptedException {
        // given
        Brand brand = brandRepository.save(new Brand("나이키"));
        ProductModel product = productRepository.save(new ProductModel(brand.getId(), "에어맥스", "설명", 100000L));
        int initialStock = 5;
        productStockRepository.save(new ProductStock(product.getId(), initialStock));

        int totalRequests = 10;
        List<Long> userIds = createUsers(totalRequests);

        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        CountDownLatch ready = new CountDownLatch(totalRequests);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < totalRequests; i++) {
            Long userId = userIds.get(i);
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    orderFacade.createOrder(userId, null,
                        List.of(new OrderFacade.OrderRequest(product.getId(), 1)));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        // then
        assertThat(successCount.get()).isEqualTo(initialStock);
        assertThat(failCount.get()).isEqualTo(totalRequests - initialStock);

        ProductStock remaining = productStockRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(remaining.getStock()).isZero();
    }

    private List<Long> createUsers(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                User user = new User("user" + i, passwordEncoder.encode("password"));
                return userRepository.save(user).getId();
            })
            .toList();
    }
}
