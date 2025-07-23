package com.online_shop.core.repository;

import com.online_shop.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryAndPriceBetween(String category, BigDecimal min, BigDecimal max);
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);
}




