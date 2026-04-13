package com.kondo.mss.product;

import java.math.BigDecimal;

public record Product(Long id, String code, String name, String category, BigDecimal unitPrice, Integer reorderPoint) {
}
