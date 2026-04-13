package com.kondo.mss.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesResponse(LocalDate saleDate, Integer orderCount, Integer totalQuantity, BigDecimal totalSales) {
}
