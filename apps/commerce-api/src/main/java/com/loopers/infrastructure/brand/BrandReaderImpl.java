package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BrandReaderImpl implements BrandReader {

    private final BrandService brandService;
    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Brand getBrand(Long brandId) {
        return brandService.getBrand(brandId);
    }

    @Override
    public Map<Long, Brand> getBrands(List<Long> brandIds) {
        return brandJpaRepository.findAllByIdIn(brandIds).stream()
            .collect(Collectors.toMap(Brand::getId, Function.identity()));
    }
}
