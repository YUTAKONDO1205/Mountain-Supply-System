package com.kondo.mss.product;

import java.util.List;

public interface ProductService {
    Product create(ProductCreateRequest request);

    Product findById(long id);

    List<Product> findAll();
}
