package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.dto.ProductCreateRequestDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductDetailResponseDto;
import com.kb.cosmetic_wms.domain.product.entity.Category;
import com.kb.cosmetic_wms.domain.product.entity.ProductType;
import com.kb.cosmetic_wms.domain.product.fixture.ProductDtoBuilder;
import com.kb.cosmetic_wms.domain.product.repository.CategoryRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductTypeRepository;
import com.kb.cosmetic_wms.domain.product.repository.SkuSequenceRepository;
import com.kb.cosmetic_wms.domain.product.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductServiceConcurrencyTest {

    @Autowired private ProductService productService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductTypeRepository productTypeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SkuSequenceRepository skuSequenceRepository;

    private Category savedCategory;
    private ProductType savedProductType;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        skuSequenceRepository.deleteAll();
        savedCategory = categoryRepository.save(Category.create("SKN", "스킨케어"));
        savedProductType = productTypeRepository.save(ProductType.create("TON", "토너"));
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        skuSequenceRepository.deleteAll();
    }

    @Test
    void 동시에_동일_그룹_상품을_N개_등록하면_SKU_순번이_중복없이_부여된다() throws InterruptedException {
        // given
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<String> skuCodes = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ProductCreateRequestDto request = new ProductDtoBuilder()
                            .categoryId(savedCategory.getId())
                            .productTypeId(savedProductType.getId())
                            .productName("토너 " + idx)
                            .build();
                    ProductDetailResponseDto result = productService.register(request);
                    skuCodes.add(result.skuCode());
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // when - 모든 스레드 동시 출발
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(finished).as("10초 내 모든 스레드가 완료되어야 한다").isTrue();
        assertThat(errors).as("예외 없이 모두 성공해야 한다 %s", errors).isEmpty();
        assertThat(skuCodes).hasSize(threadCount);
        assertThat(new HashSet<>(skuCodes))
                .as("SKU 코드가 중복 발급되어서는 안 된다")
                .hasSize(threadCount);
    }
}
