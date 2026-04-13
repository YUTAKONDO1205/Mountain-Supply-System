package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(Long orderId, String customerName, String orderStatus, BigDecimal totalAmount,
                                  LocalDateTime orderedAt, String createdBy, List<OrderItemDetail> items) {
}
