package com.kondo.mss.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotBlank(message = "商品名は必須です。")
        @Size(max = 100, message = "商品名は100文字以内で入力してください。")
        String name,

        @NotBlank(message = "カテゴリは必須です。")
        @Size(max = 50, message = "カテゴリは50文字以内で入力してください。")
        String category,

        @NotNull(message = "単価は必須です。")
        @DecimalMin(value = "0.01", message = "単価は0より大きくしてください。")
        BigDecimal unitPrice,

        @NotNull(message = "発注点は必須です。")
        @Min(value = 0, message = "発注点は0以上で入力してください。")
        Integer reorderPoint) {
}
