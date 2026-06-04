package com.kondo.mss.order;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    OrderDetailResponse create(OrderRequest request, String createdBy);

    OrderDetailResponse findById(long orderId);

    List<OrderSummaryResponse> findOrders(LocalDate from, LocalDate to, String status);

    OrderDetailResponse cancel(long orderId);
}
