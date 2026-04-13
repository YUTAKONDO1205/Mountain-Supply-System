package com.kondo.mss.report;

import java.math.BigDecimal;

public record MonthlySalesResponse(Integer salesYear, Integer salesMonth, Integer orderCount,
                                   Integer totalQuantity, BigDecimal totalSales) {
}
