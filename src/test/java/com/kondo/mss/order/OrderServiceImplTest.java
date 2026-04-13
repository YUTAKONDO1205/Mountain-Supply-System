package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kondo.mss.common.InsufficientStockException;
import com.kondo.mss.inventory.InventoryRepository;
import com.kondo.mss.product.Product;
import com.kondo.mss.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(productRepository, inventoryRepository, orderRepository);
    }

    @Test
    void create_shouldThrow_whenStockIsInsufficient() {
        OrderRequest request = new OrderRequest("テスト客", List.of(new OrderItemRequest(1L, 10)));
        Product product = new Product(1L, "FOOD-001", "フリーズドライカレー", "食品", new BigDecimal("780.00"), 20);

        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product));
        when(inventoryRepository.getCurrentStocks(List.of(1L))).thenReturn(Map.of(1L, 3));

        assertThrows(InsufficientStockException.class, () -> orderService.create(request, "staff"));
        verify(orderRepository, never()).createOrder(anyString(), anyString(), any(BigDecimal.class), any(LocalDateTime.class), anyString());
    }

    @Test
    void create_shouldReturnOrderDetail_whenRequestIsValid() {
        OrderRequest request = new OrderRequest("テスト客", List.of(new OrderItemRequest(1L, 2)));
        Product product = new Product(1L, "FOOD-001", "フリーズドライカレー", "食品", new BigDecimal("780.00"), 20);
        OrderHeader header = new OrderHeader(100L, "テスト客", "CONFIRMED", new BigDecimal("1560.00"), LocalDateTime.now(), "staff");
        OrderItemDetail detail = new OrderItemDetail(1L, "FOOD-001", "フリーズドライカレー", 2, new BigDecimal("780.00"), new BigDecimal("1560.00"));

        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product));
        when(inventoryRepository.getCurrentStocks(List.of(1L))).thenReturn(Map.of(1L, 10));
        when(orderRepository.createOrder(anyString(), anyString(), any(BigDecimal.class), any(LocalDateTime.class), anyString())).thenReturn(100L);
        when(orderRepository.findOrderHeaderById(100L)).thenReturn(java.util.Optional.of(header));
        when(orderRepository.findOrderItemsByOrderId(100L)).thenReturn(List.of(detail));

        OrderDetailResponse response = orderService.create(request, "staff");

        assertEquals(100L, response.orderId());
        assertEquals(new BigDecimal("1560.00"), response.totalAmount());
        verify(orderRepository).createOrderItem(100L, 1L, 2, new BigDecimal("780.00"), new BigDecimal("1560.00"));
        verify(inventoryRepository).createMovement(1L, "OUT", -2, "ORDER", 100L, "受注出庫");
    }
}
