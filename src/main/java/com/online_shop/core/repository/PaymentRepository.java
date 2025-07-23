package com.online_shop.core.repository;

import com.online_shop.core.entity.Order;
import com.online_shop.core.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
}