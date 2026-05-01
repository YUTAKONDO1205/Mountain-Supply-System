package com.kondo.mss.inventory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kondo.mss.common.BusinessException;
import com.kondo.mss.common.InsufficientStockException;
import com.kondo.mss.product.ProductService;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final String TYPE_IN = "IN";
    private static final String TYPE_OUT = "OUT";
    private static final Set<String> ALLOWED_TYPES = Set.of(TYPE_IN, TYPE_OUT);
    private static final String REFERENCE_TYPE_ADJUSTMENT = "ADJUSTMENT";

    private final InventoryRepository inventoryRepository;
    private final ProductService productService;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductService productService) {
        this.inventoryRepository = inventoryRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public InventoryMovement registerMovement(InventoryAdjustmentRequest request) {
        productService.findById(request.productId());
        String type = request.movementType().toUpperCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException("movementTypeはINまたはOUTで指定してください。");
        }

        if (TYPE_OUT.equals(type)) {
            int currentStock = inventoryRepository.getCurrentStock(request.productId());
            if (currentStock < request.quantity()) {
                throw new InsufficientStockException(
                        "在庫不足のため出庫できません。 currentStock=" + currentStock + ", requested=" + request.quantity());
            }
        }

        int delta = TYPE_IN.equals(type) ? request.quantity() : -request.quantity();
        LocalDateTime now = LocalDateTime.now();
        long id = inventoryRepository.createMovement(
                request.productId(), type, delta, REFERENCE_TYPE_ADJUSTMENT, null, request.note(), now);
        return new InventoryMovement(
                id, request.productId(), type, delta, REFERENCE_TYPE_ADJUSTMENT, null, request.note(), now);
    }

    @Override
    public List<InventoryStockResponse> findAllCurrentStocks() {
        return inventoryRepository.findAllCurrentStocks();
    }

    @Override
    public List<LowStockResponse> findLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }
}
