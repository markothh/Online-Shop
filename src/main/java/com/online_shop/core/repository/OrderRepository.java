package com.online_shop.core.repository;

import com.online_shop.core.entity.Order;
import com.online_shop.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}