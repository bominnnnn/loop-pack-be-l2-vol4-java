package com.loopers.concurrency;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("쿠폰 동시성 테스트")
class CouponConcurrencyTest extends IntegrationTestBase {

    @Autowired private OrderFacade orderFacade;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductStockRepository productStockRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CouponTemplateRepository couponTemplateRepository;
    @Autowired private IssuedCouponRepository issuedCouponRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @DisplayName("동일한 쿠폰으로 동시에 여러 주문이 요청되어도, 쿠폰은 단 한 번만 사용되어야 한다.")
    @Test
    void concurrentOrdersWithSameCoupon_shouldUseOnlyOnce() throws InterruptedException {
        // given
        User owner = userRepository.save(new User("couponOwner", passwordEncoder.encode("password")));

        Brand brand = brandRepository.save(new Brand("아디다스"));
        ProductModel product = productRepository.save(new ProductModel(brand.getId(), "스탠스미스", "설명", 50000L));
        productStockRepository.save(new ProductStock(product.getId(), 100));

        CouponTemplate template = couponTemplateRepository.save(
            new CouponTemplate("10% 할인", CouponType.RATE, 10L, null, ZonedDateTime.now().plusDays(30))
        );
        IssuedCoupon issuedCoupon = issuedCouponRepository.save(new IssuedCoupon(owner.getId(), template.getId()));

        int totalRequests = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        CountDownLatch ready = new CountDownLatch(totalRequests);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 같은 유저가 같은 쿠폰으로 동시에 10번 주문 시도
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    orderFacade.createOrder(owner.getId(), issuedCoupon.getId(),
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
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(totalRequests - 1);

        IssuedCoupon result = issuedCouponRepository.findById(issuedCoupon.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(IssuedCouponStatus.USED);
    }
}
