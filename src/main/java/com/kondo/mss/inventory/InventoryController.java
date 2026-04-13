package com.kondo.mss.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kondo.mss.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/api/admin/inventory/movements")
    public ResponseEntity<ApiResponse<InventoryMovement>> createMovement(@Valid @RequestBody InventoryAdjustmentRequest request) {
        InventoryMovement movement = inventoryService.registerMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("在庫移動を登録しました。", movement));
    }

    @GetMapping("/api/inventory/stocks")
    public ApiResponse<List<InventoryStockResponse>> currentStocks() {
        return new ApiResponse<>("現在庫一覧を取得しました。", inventoryService.findAllCurrentStocks());
    }

    @GetMapping("/api/inventory/low-stocks")
    public ApiResponse<List<LowStockResponse>> lowStocks() {
        return new ApiResponse<>("低在庫一覧を取得しました。", inventoryService.findLowStockProducts());
    }
}
