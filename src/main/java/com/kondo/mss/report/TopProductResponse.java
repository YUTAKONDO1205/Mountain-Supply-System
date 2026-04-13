package com.kondo.mss.report;

import java.math.BigDecimal;

public record TopProductResponse(Long productId, String productCode, String productName,
                                 Integer totalQuantity, BigDecimal totalSales) {
}
