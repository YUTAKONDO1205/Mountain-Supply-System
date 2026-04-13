package com.kondo.mss.inventory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kondo.mss.common.BusinessException;
import com.kondo.mss.product.ProductService;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Set<String> ALLOWED_TYPES = Set.of("IN", "OUT");

    private final InventoryRepository inventoryRepository;
    private final ProductService productService;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductService productService) {
        this.inventoryRepository = inventoryRepository;
        this.productService = productService;
    }

    @Override
    public InventoryMovement registerMovement(InventoryAdjustmentRequest request) {
        productService.findById(request.productId());
        String type = request.movementType().toUpperCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException("movementTypeはINまたはOUTで指定してください。");
        }

        if (type.equals("OUT")) {
            int currentStock = inventoryRepository.getCurrentStock(request.productId());
            if (currentStock < request.quantity()) {
                throw new BusinessException("在庫不足のため出庫できません。 currentStock=" + currentStock + ", requested=" + request.quantity());
            }
        }

        int delta = type.equals("IN") ? request.quantity() : -request.quantity();
        long id = inventoryRepository.createMovement(request.productId(), type, delta, "ADMIN", null, request.note());
        return new InventoryMovement(id, request.productId(), type, delta, "ADMIN", null, request.note(), LocalDateTime.now());
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
