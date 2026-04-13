package com.kondo.mss.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kondo.mss.common.BusinessException;
import com.kondo.mss.common.NotFoundException;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product create(ProductCreateRequest request) {
        if (productRepository.existsByCode(request.code())) {
            throw new BusinessException("同じ商品コードが既に存在します。");
        }
        long id = productRepository.create(request);
        return findById(id);
    }

    @Override
    public Product findById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("商品が見つかりません。 productId=" + id));
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
