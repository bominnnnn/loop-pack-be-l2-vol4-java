package com.loopers.concurrency;

import com.loopers.application.like.LikeFacade;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("좋아요 동시성 테스트")
class LikeConcurrencyTest extends IntegrationTestBase {

    @Autowired private LikeFacade likeFacade;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductStockRepository productStockRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @DisplayName("동일한 상품에 대해 여러 명이 동시에 좋아요를 요청해도, 좋아요 수가 정상 반영되어야 한다.")
    @Test
    void concurrentLikes_shouldBeCorrectlyReflected() throws InterruptedException {
        // given
        Brand brand = brandRepository.save(new Brand("뉴발란스"));
        ProductModel product = productRepository.save(new ProductModel(brand.getId(), "990v6", "설명", 200000L));
        productStockRepository.save(new ProductStock(product.getId(), 100));

        int totalUsers = 20;
        List<Long> userIds = createUsers(totalUsers);

        ExecutorService executor = Executors.newFixedThreadPool(totalUsers);
        CountDownLatch ready = new CountDownLatch(totalUsers);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        // when - 20명이 동시에 좋아요
        for (int i = 0; i < totalUsers; i++) {
            Long userId = userIds.get(i);
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    likeFacade.addLike(userId, product.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // ignore
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then
        ProductModel updated = productRepository.find(product.getId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(successCount.get());
        assertThat(updated.getLikeCount()).isEqualTo(totalUsers);
    }

    private List<Long> createUsers(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                User user = new User("likeUser" + i, passwordEncoder.encode("password"));
                return userRepository.save(user).getId();
            })
            .toList();
    }
}
