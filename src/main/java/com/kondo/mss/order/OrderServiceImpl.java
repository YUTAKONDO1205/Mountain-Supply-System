package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_CONFIRMED, STATUS_SHIPPED, STATUS_CANCELLED);

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

        productMap.values().stream()
                .filter(product -> Boolean.FALSE.equals(product.active()))
                .findFirst()
                .ifPresent(product -> {
                    throw new BusinessException("削除済みの商品は注文できません。 product=" + product.name());
                });

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
                STATUS_CONFIRMED,
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
    public List<OrderSummaryResponse> findOrders(LocalDate from, LocalDate to, String status, int page, int size) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atTime(LocalTime.MAX);

        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            normalizedStatus = status.toUpperCase(Locale.ROOT);
            if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
                throw new BusinessException("statusはCONFIRMED / SHIPPED / CANCELLEDのいずれかで指定してください。");
            }
        }

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        int offset = safePage * safeSize;

        return orderRepository.findOrderSummaries(fromDateTime, toDateTime, normalizedStatus, safeSize, offset);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancel(long orderId) {
        OrderHeader header = orderRepository.findOrderHeaderById(orderId)
                .orElseThrow(() -> new NotFoundException("注文が見つかりません。 orderId=" + orderId));

        if (STATUS_CANCELLED.equals(header.orderStatus())) {
            throw new BusinessException("この注文は既にキャンセル済みです。 orderId=" + orderId);
        }
        if (STATUS_SHIPPED.equals(header.orderStatus())) {
            throw new BusinessException("出荷済みの注文はキャンセルできません。 orderId=" + orderId);
        }

        List<OrderItemDetail> items = orderRepository.findOrderItemsByOrderId(orderId);
        items.forEach(item -> inventoryRepository.createMovement(
                item.productId(), "IN", item.quantity(), "ORDER_CANCEL", orderId, "受注キャンセルによる戻入"));

        orderRepository.updateStatus(orderId, STATUS_CANCELLED);
        return findById(orderId);
    }

    @Override
    @Transactional
    public OrderDetailResponse ship(long orderId) {
        OrderHeader header = orderRepository.findOrderHeaderById(orderId)
                .orElseThrow(() -> new NotFoundException("注文が見つかりません。 orderId=" + orderId));

        if (!STATUS_CONFIRMED.equals(header.orderStatus())) {
            throw new BusinessException(
                    "確定済み(CONFIRMED)の注文のみ出荷できます。 orderId=" + orderId + ", status=" + header.orderStatus());
        }

        orderRepository.updateStatus(orderId, STATUS_SHIPPED);
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
