package com.loopers.support;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * local 프로파일에서만 실행되는 10만 건 상품 데이터 초기화.
 * 인덱스 성능 테스트 및 EXPLAIN 분석용.
 */
@Slf4j
@Profile("local")
@RequiredArgsConstructor
@Component
public class DataInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final BrandRepository brandRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        Long productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Long.class);
        if (productCount != null && productCount > 0) {
            log.info("[DataInitializer] 상품 데이터가 이미 존재합니다. 초기화를 건너뜁니다. ({}건)", productCount);
            return;
        }

        log.info("[DataInitializer] 상품 10만 건 초기화 시작...");
        long start = System.currentTimeMillis();

        // 브랜드 10개 생성
        List<Long> brandIds = new ArrayList<>();
        String[] brandNames = {"나이키", "아디다스", "뉴발란스", "리복", "푸마", "컨버스", "반스", "살로몬", "호카", "아식스"};
        for (String name : brandNames) {
            Brand brand = brandRepository.save(new Brand(name));
            brandIds.add(brand.getId());
        }

        // 상품 + 재고 배치 INSERT (JDBC batch for performance)
        Random random = new Random();
        int batchSize = 1000;
        int total = 100_000;

        List<Object[]> productBatch = new ArrayList<>(batchSize);
        for (int i = 1; i <= total; i++) {
            Long brandId = brandIds.get(i % brandIds.size());
            long price   = (random.nextInt(100) + 1) * 1000L; // 1,000 ~ 100,000
            int likeCount = random.nextInt(10_000);
            productBatch.add(new Object[]{brandId, "상품_" + i, "설명_" + i, price, likeCount});

            if (productBatch.size() == batchSize || i == total) {
                jdbcTemplate.batchUpdate(
                    "INSERT INTO products (brand_id, name, description, price, like_count, created_at, updated_at, deleted_at) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW(), NULL)",
                    productBatch
                );
                productBatch.clear();
            }
        }

        // 재고 배치 INSERT (product_id 범위로)
        Long minId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM products", Long.class);
        if (minId == null) return;

        List<Object[]> stockBatch = new ArrayList<>(batchSize);
        for (int i = 0; i < total; i++) {
            int stock = random.nextInt(500) + 10;
            stockBatch.add(new Object[]{minId + i, stock});

            if (stockBatch.size() == batchSize || i == total - 1) {
                jdbcTemplate.batchUpdate(
                    "INSERT INTO product_stocks (product_id, stock, created_at, updated_at, deleted_at) " +
                    "VALUES (?, ?, NOW(), NOW(), NULL)",
                    stockBatch
                );
                stockBatch.clear();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[DataInitializer] 초기화 완료: {}건, {}ms", total, elapsed);
    }
}
