package com.kondo.mss.order;

public interface OrderService {
    OrderDetailResponse create(OrderRequest request, String createdBy);

    OrderDetailResponse findById(long orderId);
}
