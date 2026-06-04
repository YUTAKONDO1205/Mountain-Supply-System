package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(Long orderId, String customerName, String orderStatus,
                                   BigDecimal totalAmount, LocalDateTime orderedAt, String createdBy,
                                   Integer itemCount) {
}
