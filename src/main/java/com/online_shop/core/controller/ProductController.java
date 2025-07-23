package com.online_shop.core.controller;

import com.online_shop.core.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(required = false, defaultValue = "1000000") BigDecimal maxPrice,
            Model model
    ) {
        model.addAttribute("products", productService.getFilteredProducts(category, minPrice, maxPrice));
        model.addAttribute("categories", productService.getAllCategories());
        return "index";
    }

    @GetMapping("/products")
    public String showProductDetails(@RequestParam("id") Long id, Model model, Principal principal) {
        return productService.getProductById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("isLoggedIn", principal != null);
                    return "product";
                })
                .orElseGet(() -> {
                    System.out.println("Продукт с id=" + id + " не найден");
                    return "redirect:/products";
                });
    }
}

