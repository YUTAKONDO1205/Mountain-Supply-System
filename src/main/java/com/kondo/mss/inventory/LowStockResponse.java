package com.kondo.mss.inventory;

public record LowStockResponse(Long productId, String code, String name, Integer currentStock,
                               Integer reorderPoint, Integer recent30DaySales) {
}
