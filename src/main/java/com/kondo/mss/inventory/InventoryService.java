package com.kondo.mss.inventory;

import java.util.List;

public interface InventoryService {
    InventoryMovement registerMovement(InventoryAdjustmentRequest request);

    List<InventoryMovement> findMovements(Long productId, int limit);

    List<InventoryStockResponse> findAllCurrentStocks();

    List<LowStockResponse> findLowStockProducts();
}
