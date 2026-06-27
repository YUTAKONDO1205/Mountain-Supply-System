package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kondo.mss.common.BusinessException;
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
        Product product = new Product(1L, "FOOD-001", "フリーズドライカレー", "食品", new BigDecimal("780.00"), 20, true);

        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product));
        when(inventoryRepository.getCurrentStocks(List.of(1L))).thenReturn(Map.of(1L, 3));

        assertThrows(InsufficientStockException.class, () -> orderService.create(request, "staff"));
        verify(orderRepository, never()).createOrder(anyString(), anyString(), any(BigDecimal.class), any(LocalDateTime.class), anyString());
    }

    @Test
    void create_shouldReturnOrderDetail_whenRequestIsValid() {
        OrderRequest request = new OrderRequest("テスト客", List.of(new OrderItemRequest(1L, 2)));
        Product product = new Product(1L, "FOOD-001", "フリーズドライカレー", "食品", new BigDecimal("780.00"), 20, true);
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

    @Test
    void cancel_shouldRestockAndMarkCancelled_whenOrderIsConfirmed() {
        OrderHeader header = new OrderHeader(50L, "テスト客", "CONFIRMED", new BigDecimal("1560.00"), LocalDateTime.now(), "staff");
        OrderItemDetail item = new OrderItemDetail(1L, "FOOD-001", "フリーズドライカレー", 2, new BigDecimal("780.00"), new BigDecimal("1560.00"));

        when(orderRepository.findOrderHeaderById(50L)).thenReturn(Optional.of(header));
        when(orderRepository.findOrderItemsByOrderId(50L)).thenReturn(List.of(item));

        orderService.cancel(50L);

        verify(inventoryRepository).createMovement(1L, "IN", 2, "ORDER_CANCEL", 50L, "受注キャンセルによる戻入");
        verify(orderRepository).updateStatus(50L, "CANCELLED");
    }

    @Test
    void cancel_shouldThrow_whenOrderIsAlreadyCancelled() {
        OrderHeader header = new OrderHeader(50L, "テスト客", "CANCELLED", new BigDecimal("1560.00"), LocalDateTime.now(), "staff");
        when(orderRepository.findOrderHeaderById(50L)).thenReturn(Optional.of(header));

        assertThrows(BusinessException.class, () -> orderService.cancel(50L));
        verify(orderRepository, never()).updateStatus(anyLong(), anyString());
        verify(inventoryRepository, never())
                .createMovement(anyLong(), anyString(), anyInt(), anyString(), any(), anyString());
    }

    @Test
    void findOrders_shouldThrow_whenStatusIsInvalid() {
        assertThrows(BusinessException.class, () -> orderService.findOrders(null, null, "DRAFT", 0, 20));
    }

    @Test
    void ship_shouldMarkShipped_whenOrderIsConfirmed() {
        OrderHeader header = new OrderHeader(50L, "テスト客", "CONFIRMED", new BigDecimal("1560.00"), LocalDateTime.now(), "staff");
        when(orderRepository.findOrderHeaderById(50L)).thenReturn(Optional.of(header));
        when(orderRepository.findOrderItemsByOrderId(50L)).thenReturn(List.of());

        orderService.ship(50L);

        verify(orderRepository).updateStatus(50L, "SHIPPED");
    }

    @Test
    void ship_shouldThrow_whenOrderIsNotConfirmed() {
        OrderHeader header = new OrderHeader(50L, "テスト客", "CANCELLED", new BigDecimal("1560.00"), LocalDateTime.now(), "staff");
        when(orderRepository.findOrderHeaderById(50L)).thenReturn(Optional.of(header));

        assertThrows(BusinessException.class, () -> orderService.ship(50L));
        verify(orderRepository, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void cancel_shouldThrow_whenOrderIsAlreadyShipped() {
        OrderHeader header = new OrderHeader(50L, "テスト客", "SHIPPED", new BigDecimal("1560.00"), LocalDateTime.now(), "staff");
        when(orderRepository.findOrderHeaderById(50L)).thenReturn(Optional.of(header));

        assertThrows(BusinessException.class, () -> orderService.cancel(50L));
        verify(orderRepository, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void create_shouldThrow_whenProductIsDeleted() {
        OrderRequest request = new OrderRequest("テスト客", List.of(new OrderItemRequest(1L, 1)));
        Product inactive = new Product(1L, "FOOD-001", "フリーズドライカレー", "食品", new BigDecimal("780.00"), 20, false);
        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(inactive));

        assertThrows(BusinessException.class, () -> orderService.create(request, "staff"));
        verify(orderRepository, never()).createOrder(anyString(), anyString(), any(BigDecimal.class), any(LocalDateTime.class), anyString());
    }
}
