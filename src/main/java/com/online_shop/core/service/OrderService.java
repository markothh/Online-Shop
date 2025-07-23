package com.online_shop.core.service;

import com.online_shop.core.entity.Order;
import com.online_shop.core.entity.User;
import com.online_shop.core.repository.OrderRepository;
import com.online_shop.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getUserOrders(String filter) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("Пользователь не найден: {}", username);
            throw new IllegalArgumentException("Пользователь не найден: " + username);
        }
        List<Order> orders = orderRepository.findByUser(user);
        if (filter == null || "all".equalsIgnoreCase(filter)) {
            return orders;
        } else if ("current".equalsIgnoreCase(filter)) {
            return orders.stream()
                    .filter(order -> order.getShipment() != null &&
                            !("delivered".equalsIgnoreCase(order.getShipment().getStatus())))
                    .collect(Collectors.toList());
        } else if ("unpaid".equalsIgnoreCase(filter)) {
            return orders.stream()
                    .filter(order -> !order.isPaid())
                    .collect(Collectors.toList());
        }
        return orders;
    }

    public Order getOrderDetails(Long orderId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("Пользователь не найден: {}", username);
            throw new IllegalArgumentException("Пользователь не найден: " + username);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            logger.error("Несанкционированный доступ к заказу {} пользователем {}", orderId, username);
            throw new IllegalArgumentException("Несанкционированный доступ к заказу");
        }
        // Инициализация лениво загружаемых коллекций
        order.getOrderItems().size(); // Принудительная загрузка orderItems
        if (order.getShipment() != null) {
            order.getShipment().getPickupPoint(); // Принудительная загрузка pickupPoint
        }
        return order;
    }
}