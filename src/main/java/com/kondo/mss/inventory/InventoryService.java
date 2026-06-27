package com.kondo.mss.inventory;

import java.util.List;

public interface InventoryService {
    InventoryMovement registerMovement(InventoryAdjustmentRequest request);

    List<InventoryMovement> findMovements(Long productId, int limit);

    List<InventoryStockResponse> findAllCurrentStocks();

    InventoryStockResponse findStock(long productId);

    List<LowStockResponse> findLowStockProducts();
}
