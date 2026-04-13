package com.kondo.mss.inventory;

import java.util.List;

public interface InventoryService {
    InventoryMovement registerMovement(InventoryAdjustmentRequest request);

    List<InventoryStockResponse> findAllCurrentStocks();

    List<LowStockResponse> findLowStockProducts();
}
