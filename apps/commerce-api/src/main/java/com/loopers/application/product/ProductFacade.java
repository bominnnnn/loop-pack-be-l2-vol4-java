package com.loopers.application.product;

import com.loopers.config.CacheConfig;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;
import com.loopers.domain.product.ProductStockService;
import com.loopers.domain.product.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final ProductStockService productStockService;
    private final ProductStockRepository productStockRepository;
    private final BrandReader brandReader;

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public ProductInfo createProduct(Long brandId, String name, String description, Long price, int initialStock) {
        ProductModel product = productService.createProduct(brandId, name, description, price);
        ProductStock stock = productStockService.createStock(product.getId(), initialStock);
        Brand brand = brandReader.getBrand(brandId);
        return ProductInfo.of(product, stock, brand);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#id")
    public ProductInfo getProduct(Long id) {
        ProductModel product = productService.getProduct(id);
        ProductStock stock = productStockService.getStock(id);
        Brand brand = brandReader.getBrand(product.getBrandId());
        return ProductInfo.of(product, stock, brand);
    }

    /**
     * N+1 개선: product 목록 조회 후 stock/brand를 각각 IN 쿼리로 배치 조회.
     * 캐시 키: sort + brandId 조합 (brandId null이면 "all")
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.PRODUCT_LIST, key = "#sort.name() + '::' + (#brandId ?: 'all')")
    public List<ProductInfo> getAllProducts(SortType sort, Long brandId) {
        List<ProductModel> products = productService.getAllProducts(sort, brandId);
        if (products.isEmpty()) return List.of();

        List<Long> productIds = products.stream().map(ProductModel::getId).toList();
        List<Long> brandIds   = products.stream().map(ProductModel::getBrandId).distinct().toList();

        Map<Long, ProductStock> stockMap = productStockRepository.findAllByProductIds(productIds);
        Map<Long, Brand> brandMap        = brandReader.getBrands(brandIds);

        return products.stream()
            .map(p -> ProductInfo.of(p, stockMap.get(p.getId()), brandMap.get(p.getBrandId())))
            .toList();
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#id"),
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public ProductInfo updateProduct(Long id, String name, String description, Long price) {
        ProductModel product = productService.updateProduct(id, name, description, price);
        ProductStock stock = productStockService.getStock(id);
        Brand brand = brandReader.getBrand(product.getBrandId());
        return ProductInfo.of(product, stock, brand);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#id"),
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public void deleteProduct(Long id) {
        productService.deleteProduct(id);
    }

    /**
     * 좋아요 변경 시 상품 상세 캐시 무효화.
     * likeCount가 바뀌므로 캐시된 ProductInfo가 stale해진다.
     */
    @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId")
    public void evictProductCache(Long productId) {
    }
}
