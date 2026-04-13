package com.kondo.mss.inventory;

public record InventoryStockResponse(Long productId, String code, String name, String category,
                                     Integer reorderPoint, Integer currentStock) {
}
