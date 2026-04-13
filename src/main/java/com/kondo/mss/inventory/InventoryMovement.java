package com.kondo.mss.inventory;

import java.time.LocalDateTime;

public record InventoryMovement(Long id, Long productId, String movementType, Integer quantityDelta,
                                String referenceType, Long referenceId, String note, LocalDateTime createdAt) {
}
