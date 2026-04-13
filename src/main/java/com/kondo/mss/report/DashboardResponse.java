package com.kondo.mss.report;

import java.math.BigDecimal;

public record DashboardResponse(Integer productCount, Integer lowStockCount, BigDecimal todaySales) {
}
