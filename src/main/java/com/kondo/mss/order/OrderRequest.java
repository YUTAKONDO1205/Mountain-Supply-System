package com.kondo.mss.order;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotBlank(message = "customerNameは必須です。")
        @Size(max = 100, message = "customerNameは100文字以内で入力してください。")
        String customerName,

        @NotEmpty(message = "itemsは1件以上指定してください。")
        List<@Valid OrderItemRequest> items) {
}
