package com.kondo.mss.report;

import java.math.BigDecimal;

public record CategorySalesResponse(String category, Integer totalQuantity, BigDecimal totalSales) {
}
