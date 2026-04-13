package com.kondo.mss.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryAdjustmentRequest(
        @NotNull(message = "productIdは必須です。")
        Long productId,

        @NotBlank(message = "movementTypeは必須です。")
        String movementType,

        @NotNull(message = "quantityは必須です。")
        @Min(value = 1, message = "quantityは1以上で入力してください。")
        Integer quantity,

        @Size(max = 255, message = "noteは255文字以内で入力してください。")
        String note) {
}
