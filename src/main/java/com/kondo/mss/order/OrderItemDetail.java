package com.kondo.mss.order;

import java.math.BigDecimal;

public record OrderItemDetail(Long productId, String productCode, String productName,
                              Integer quantity, BigDecimal unitPrice, BigDecimal lineAmount) {
}
