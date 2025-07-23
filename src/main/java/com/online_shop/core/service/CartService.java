package com.online_shop.core.service;

import com.online_shop.core.entity.*;
import com.online_shop.core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final PickupPointRepository pickupPointRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       UserRepository userRepository, OrderRepository orderRepository,
                       ShipmentRepository shipmentRepository, PickupPointRepository pickupPointRepository,
                       OrderItemRepository orderItemRepository, PaymentRepository paymentRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.pickupPointRepository = pickupPointRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void addToCart(Long productId, int quantity) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Adding product {} to cart for user {}, quantity: {}", productId, username, quantity);

        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Cart existingCart = cartRepository.findByUserAndProduct(user, product);
        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            cartRepository.save(existingCart);
            logger.info("Updated cart item: product {}, new quantity: {}", productId, existingCart.getQuantity());
        } else {
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(quantity);
            cartRepository.save(cart);
            logger.info("Added new cart item: product {}, quantity: {}", productId, quantity);
        }
    }

    public List<Cart> getCartItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }
        return cartRepository.findByUser(user);
    }

    @Transactional
    public void updateCartItemQuantity(Long cartId, int quantity) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Updating cart item {} for user {}, new quantity: {}", cartId, username, quantity);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartId));

        User user = userRepository.findByUsername(username);
        if (user == null || !cart.getUser().getId().equals(user.getId())) {
            logger.error("Unauthorized access to cart item {} by user {}", cartId, username);
            throw new IllegalArgumentException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cartRepository.delete(cart);
            logger.info("Deleted cart item {} for user {}", cartId, username);
        } else {
            cart.setQuantity(quantity);
            cartRepository.save(cart);
            logger.info("Updated cart item {} quantity to {}", cartId, quantity);
        }
    }

    @Transactional
    public void deleteSelectedCartItems(List<Long> cartIds) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Deleting cart items {} for user {}", cartIds, username);

        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        for (Long cartId : cartIds) {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartId));
            if (!cart.getUser().getId().equals(user.getId())) {
                logger.error("Unauthorized access to cart item {} by user {}", cartId, username);
                throw new IllegalArgumentException("Unauthorized access to cart item");
            }
            cartRepository.delete(cart);
            logger.info("Deleted cart item {} for user {}", cartId, username);
        }
    }

    @Transactional
    public void placeOrder(List<Long> cartItemIds, Long pickupPointId, Long cardId, boolean payLater) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        PickupPoint pickupPoint = pickupPointRepository.findById(pickupPointId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup point not found: " + pickupPointId));

        List<Cart> selectedItems = cartRepository.findAllById(cartItemIds);
        if (selectedItems.isEmpty()) {
            logger.error("No items selected for order");
            throw new IllegalArgumentException("No items selected for order");
        }

        // Создаем отправку
        Shipment shipment = new Shipment();
        shipment.setStatus("registered");
        shipment.setShipmentDate(LocalDateTime.now());
        shipment.setPickupPoint(pickupPoint);
        shipmentRepository.save(shipment);

        // Создаем заказ
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);
        order.setShipment(shipment);
        order.setPaid(false); // Триггер управляет isPaid
        orderRepository.save(order);

        // Создаем запись в payments
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentStatus(payLater ? "pending" : (cardId != null ? "completed" : "pending"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setMethod(payLater ? "later" : (cardId != null ? "card" : "later"));
        payment.setCardId(payLater ? null : cardId);
        paymentRepository.save(payment);

        // Сохраняем товары в order_items и обновляем склад
        for (Cart cart : selectedItems) {
            Product product = cart.getProduct();
            int newStock = product.getStock() - cart.getQuantity();
            if (newStock < 0) {
                logger.error("Insufficient stock for product: {}", product.getTitle());
                throw new IllegalStateException("Insufficient stock for product: " + product.getTitle());
            }
            product.setStock(newStock);
            productRepository.save(product);

            // Добавляем в order_items
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cart.getQuantity());
            orderItemRepository.save(orderItem);

            // Удаляем из корзины
            cartRepository.delete(cart);
        }

        logger.info("Order placed for user {}, orderId: {}", username, order.getId());
    }
}