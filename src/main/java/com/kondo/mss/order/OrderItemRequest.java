package com.kondo.mss.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "productIdは必須です。")
        Long productId,

        @NotNull(message = "quantityは必須です。")
        @Min(value = 1, message = "quantityは1以上で入力してください。")
        Integer quantity) {
}
