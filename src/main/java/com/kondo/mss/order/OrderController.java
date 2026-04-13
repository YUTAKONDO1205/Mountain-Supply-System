package com.kondo.mss.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kondo.mss.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> create(@Valid @RequestBody OrderRequest request,
                                                                   Authentication authentication) {
        OrderDetailResponse response = orderService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("注文を登録しました。", response));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> findById(@PathVariable long orderId) {
        return new ApiResponse<>("注文詳細を取得しました。", orderService.findById(orderId));
    }
}
