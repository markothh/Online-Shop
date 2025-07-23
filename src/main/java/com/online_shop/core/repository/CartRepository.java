package com.online_shop.core.repository;

import com.online_shop.core.entity.Cart;
import com.online_shop.core.entity.Product;
import com.online_shop.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserAndProduct(User user, Product product);
    List<Cart> findByUser(User user);
}