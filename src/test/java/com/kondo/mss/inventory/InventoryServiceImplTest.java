package com.kondo.mss.inventory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kondo.mss.common.BusinessException;
import com.kondo.mss.common.InsufficientStockException;
import com.kondo.mss.product.ProductService;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductService productService;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, productService);
    }

    @Test
    void registerMovement_shouldThrow_whenTypeIsInvalid() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(1L, "MOVE", 5, null);

        assertThrows(BusinessException.class, () -> inventoryService.registerMovement(request));
    }

    @Test
    void registerMovement_shouldThrow_whenOutExceedsCurrentStock() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(1L, "OUT", 10, "出庫");
        when(inventoryRepository.getCurrentStock(1L)).thenReturn(3);

        assertThrows(InsufficientStockException.class, () -> inventoryService.registerMovement(request));
    }

    @Test
    void registerMovement_shouldCreateInMovement_whenTypeIsIn() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(1L, "in", 30, "追加納品");
        when(inventoryRepository.createMovement(
                eq(1L), eq("IN"), eq(30), eq("ADJUSTMENT"), isNull(), eq("追加納品"), any()))
                .thenReturn(100L);

        InventoryMovement result = inventoryService.registerMovement(request);

        assertEquals(100L, result.id());
        assertEquals("IN", result.movementType());
        assertEquals(30, result.quantityDelta());
    }

    @Test
    void findMovements_shouldClampLimit_andVerifyProductExists() {
        when(inventoryRepository.findMovements(1L, 200)).thenReturn(List.of());

        inventoryService.findMovements(1L, 9999);

        verify(productService).findById(1L);
        verify(inventoryRepository).findMovements(1L, 200);
    }
}
