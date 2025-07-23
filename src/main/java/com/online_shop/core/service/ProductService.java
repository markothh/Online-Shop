package com.online_shop.core.service;

import com.online_shop.core.entity.Product;
import com.online_shop.core.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getFilteredProducts(String category, BigDecimal minPrice, BigDecimal maxPrice) {
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategoryAndPriceBetween(category, minPrice, maxPrice);
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<String> getAllCategories() {
        return productRepository.findAll()
                .stream()
                .map(Product::getCategory)
                .distinct()
                .toList();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
}

