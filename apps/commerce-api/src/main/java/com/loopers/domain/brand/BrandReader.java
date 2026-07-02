package com.loopers.domain.brand;

import java.util.List;
import java.util.Map;

/**
 * 타 도메인(Order 등)이 Brand 정보를 읽을 때 사용하는 읽기 전용 창구.
 */
public interface BrandReader {
    Brand getBrand(Long brandId);
    Map<Long, Brand> getBrands(List<Long> brandIds);
}
