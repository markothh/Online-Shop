package com.online_shop.core.controller;

import com.online_shop.core.entity.UserCard;
import com.online_shop.core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final UserCardService userCardService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(UserCardService userCardService, OrderService orderService, PaymentService paymentService) {
        this.userCardService = userCardService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping("/orders")
    public String showOrders(Model model,
                             @RequestParam(required = false) Boolean orderSuccess,
                             @RequestParam(required = false) String filter) {
        logger.info("Отображение заказов для пользователя с фильтром: {}", filter);
        model.addAttribute("orders", orderService.getUserOrders(filter));
        model.addAttribute("currentFilter", filter != null ? filter : "all");
        if (orderSuccess != null && orderSuccess) {
            model.addAttribute("successMessage", "Заказ успешно оформлен!");
        }
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String showOrderDetails(@PathVariable Long id, Model model) {
        logger.info("Displaying details for order {}", id);
        try {
            model.addAttribute("order", orderService.getOrderDetails(id));
            return "order-details";
        } catch (IllegalArgumentException e) {
            logger.error("Error accessing order {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "orders";
        }
    }

    @GetMapping("/orders/{id}/pay")
    public String showPaymentPage(@PathVariable Long id, Model model) {
        logger.info("Доступ к странице оплаты для заказа {}", id);
        try {
            orderService.getOrderDetails(id); // Проверяем доступ к заказу
            model.addAttribute("orderId", id);
            return "payment";
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка доступа к заказу {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "orders";
        }
    }

    @PostMapping("/orders/{id}/pay")
    public String processPayment(@PathVariable Long id,
                                 @RequestParam String cardNumber,
                                 @RequestParam String expiryMonth,
                                 @RequestParam String expiryYear,
                                 @RequestParam String cvv,
                                 Model model) {
        logger.info("Обработка оплаты для заказа {}", id);
        try {
            // Валидация данных карты
            cardNumber = cardNumber.replaceAll("\\s", "");
            if (!cardNumber.matches("\\d{16}")) {
                throw new IllegalArgumentException("Номер карты должен содержать 16 цифр");
            }
            int month = Integer.parseInt(expiryMonth);
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Месяц должен быть от 1 до 12");
            }
            int year = Integer.parseInt(expiryYear);
            if (year < 2025) {
                throw new IllegalArgumentException("Год должен быть не ранее 2025");
            }
            if (!cvv.matches("\\d{3}")) {
                throw new IllegalArgumentException("CVV должен содержать 3 цифры");
            }

            // Сохранение карты
            UserCard card = userCardService.saveCard(cardNumber, month, year);

            // Завершение платежа
            paymentService.completePayment(id, card.getId());

            model.addAttribute("success", "Оплата успешно завершена");
            model.addAttribute("orderId", id);
            return "payment";
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка обработки оплаты для заказа {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("orderId", id);
            return "payment";
        }
    }
}
