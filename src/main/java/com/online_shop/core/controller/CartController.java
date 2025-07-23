package com.online_shop.core.controller;

import com.online_shop.core.service.CartService;
import com.online_shop.core.service.PickupPointService;
import com.online_shop.core.service.UserCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;
    private final PickupPointService pickupPointService;
    private final UserCardService userCardService;

    public CartController(CartService cartService, PickupPointService pickupPointService, UserCardService userCardService) {
        this.cartService = cartService;
        this.pickupPointService = pickupPointService;
        this.userCardService = userCardService;
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity, Model model) {
        logger.info("Received add to cart request: productId={}, quantity={}", productId, quantity);
        try {
            cartService.addToCart(productId, quantity);
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            logger.error("Error adding to cart: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/product?error";
        }
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        logger.info("Displaying cart for user");
        model.addAttribute("cartItems", cartService.getCartItems());
        return "cart";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam Long cartId, @RequestParam int quantity, Model model) {
        logger.info("Received update cart request: cartId={}, quantity={}", cartId, quantity);
        try {
            cartService.updateCartItemQuantity(cartId, quantity);
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            logger.error("Error updating cart: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart?error";
        }
    }

    @PostMapping("/cart/delete-selected")
    public String deleteSelectedItems(@RequestParam("cartIds") List<Long> cartIds, Model model) {
        logger.info("Received delete selected items request: cartIds={}", cartIds);
        try {
            cartService.deleteSelectedCartItems(cartIds);
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting selected items: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart?error";
        }
    }

    @GetMapping("/cart/checkout")
    public String showCheckoutForm(Model model) {
        logger.info("Displaying checkout form");
        model.addAttribute("pickupPoints", pickupPointService.getAllPickupPoints());
        model.addAttribute("userCards", userCardService.getUserCards());
        return "checkout";
    }

    @PostMapping("/cart/place-order")
    public String placeOrder(@RequestParam List<Long> cartItemIds,
                             @RequestParam Long pickupPointId,
                             @RequestParam(required = false) Long cardId,
                             @RequestParam(defaultValue = "false") boolean payLater,
                             Model model) {
        logger.info("Placing order with cartItemIds: {}, pickupPointId: {}, cardId: {}, payLater: {}",
                cartItemIds, pickupPointId, cardId, payLater);
        try {
            cartService.placeOrder(cartItemIds, pickupPointId, cardId, payLater);
            return "redirect:/orders?orderSuccess=true";
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error placing order: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "checkout";
        }
    }
}