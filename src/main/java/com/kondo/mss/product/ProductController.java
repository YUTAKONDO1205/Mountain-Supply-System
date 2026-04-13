package com.kondo.mss.product;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kondo.mss.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/api/admin/products")
    public ResponseEntity<ApiResponse<Product>> create(@Valid @RequestBody ProductCreateRequest request) {
        Product created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("商品を登録しました。", created));
    }

    @GetMapping("/api/products")
    public ApiResponse<List<Product>> findAll() {
        return new ApiResponse<>("商品一覧を取得しました。", productService.findAll());
    }

    @GetMapping("/api/products/{id}")
    public ApiResponse<Product> findById(@PathVariable long id) {
        return new ApiResponse<>("商品詳細を取得しました。", productService.findById(id));
    }
}
