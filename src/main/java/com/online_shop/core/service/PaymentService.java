package com.online_shop.core.service;

import com.online_shop.core.entity.Order;
import com.online_shop.core.entity.Payment;
import com.online_shop.core.repository.OrderRepository;
import com.online_shop.core.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public void completePayment(Long orderId, Long cardId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("Платеж для заказа не найден: " + orderId));

        payment.setPaymentStatus("completed");
        payment.setMethod("card");
        payment.setCardId(cardId);
        paymentRepository.save(payment);

        logger.info("Платеж для заказа {} завершен, cardId: {}", orderId, cardId);
    }
}