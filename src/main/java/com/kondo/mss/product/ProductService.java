package com.kondo.mss.product;

import java.util.List;

public interface ProductService {
    Product create(ProductCreateRequest request);

    Product update(long id, ProductUpdateRequest request);

    Product delete(long id);

    Product findById(long id);

    List<Product> findAll();
}
