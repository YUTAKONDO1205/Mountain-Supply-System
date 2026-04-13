package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kondo.mss.common.BusinessException;
import com.kondo.mss.common.InsufficientStockException;
import com.kondo.mss.common.NotFoundException;
import com.kondo.mss.inventory.InventoryRepository;
import com.kondo.mss.product.Product;
import com.kondo.mss.product.ProductRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(ProductRepository productRepository,
                            InventoryRepository inventoryRepository,
                            OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderDetailResponse create(OrderRequest request, String createdBy) {
        List<Long> productIds = request.items().stream()
                .map(OrderItemRequest::productId)
                .toList();

        long distinctCount = productIds.stream().distinct().count();
        if (productIds.size() != distinctCount) {
            throw new BusinessException("同じ商品を複数行に分けず、1行にまとめてください。");
        }

        Map<Long, Product> productMap = productRepository.findByIds(productIds).stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));

        if (productMap.size() != distinctCount) {
            throw new BusinessException("存在しない商品が含まれています。");
        }

        Map<Long, Integer> stockMap = inventoryRepository.getCurrentStocks(productIds);

        request.items().forEach(item -> {
            int currentStock = stockMap.getOrDefault(item.productId(), 0);
            if (currentStock < item.quantity()) {
                Product product = productMap.get(item.productId());
                throw new InsufficientStockException(
                        "在庫不足です。 product=" + product.name() + ", currentStock=" + currentStock + ", requested=" + item.quantity());
            }
        });

        BigDecimal totalAmount = request.items().stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());
                    return product.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderId = orderRepository.createOrder(
                request.customerName(),
                "CONFIRMED",
                totalAmount,
                LocalDateTime.now(),
                createdBy);

        request.items().forEach(item -> {
            Product product = productMap.get(item.productId());
            BigDecimal lineAmount = product.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            orderRepository.createOrderItem(orderId, item.productId(), item.quantity(), product.unitPrice(), lineAmount);
            inventoryRepository.createMovement(item.productId(), "OUT", -item.quantity(), "ORDER", orderId, "受注出庫");
        });

        return findById(orderId);
    }

    @Override
    public OrderDetailResponse findById(long orderId) {
        OrderHeader header = orderRepository.findOrderHeaderById(orderId)
                .orElseThrow(() -> new NotFoundException("注文が見つかりません。 orderId=" + orderId));
        List<OrderItemDetail> items = orderRepository.findOrderItemsByOrderId(orderId);
        return new OrderDetailResponse(
                header.id(),
                header.customerName(),
                header.orderStatus(),
                header.totalAmount(),
                header.orderedAt(),
                header.createdBy(),
                items);
    }
}
