package com.kondo.mss.product;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kondo.mss.common.BusinessException;
import com.kondo.mss.common.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void create_shouldThrow_whenCodeAlreadyExists() {
        ProductCreateRequest request = new ProductCreateRequest("FOOD-001", "重複商品", "食品", new BigDecimal("100.00"), 5);
        when(productRepository.existsByCode("FOOD-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> productService.create(request));
        verify(productRepository, never()).create(any());
    }

    @Test
    void create_shouldReturnCreatedProduct_whenCodeIsNew() {
        ProductCreateRequest request = new ProductCreateRequest("FOOD-099", "新商品", "食品", new BigDecimal("100.00"), 5);
        Product saved = new Product(7L, "FOOD-099", "新商品", "食品", new BigDecimal("100.00"), 5);

        when(productRepository.existsByCode("FOOD-099")).thenReturn(false);
        when(productRepository.create(request)).thenReturn(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(saved));

        Product result = productService.create(request);

        assertEquals(7L, result.id());
        assertEquals("新商品", result.name());
    }

    @Test
    void update_shouldReturnUpdatedProduct_whenProductExists() {
        ProductUpdateRequest request = new ProductUpdateRequest("改名後", "装備", new BigDecimal("999.00"), 9);
        Product existing = new Product(3L, "GEAR-001", "ガスカートリッジ", "装備", new BigDecimal("650.00"), 15);
        Product updated = new Product(3L, "GEAR-001", "改名後", "装備", new BigDecimal("999.00"), 9);

        when(productRepository.findById(3L)).thenReturn(Optional.of(existing), Optional.of(updated));

        Product result = productService.update(3L, request);

        assertEquals("改名後", result.name());
        assertEquals(new BigDecimal("999.00"), result.unitPrice());
        verify(productRepository).update(3L, request);
    }

    @Test
    void findById_shouldThrow_whenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.findById(999L));
    }
}
