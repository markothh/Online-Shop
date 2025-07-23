package com.online_shop.core.controller;

import com.online_shop.core.entity.User;
import com.online_shop.core.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/seller/analytics")
    public String showAnalyticsOverview() {
        return "seller/analytics";
    }

    @GetMapping("/seller/analytics/monthly")
    public String getMonthlyAnalytics(Model model, @AuthenticationPrincipal User userDetails) {
        model.addAttribute("sales", analyticsService.getMonthlySales(userDetails.getId()));
        return "seller/analytics-monthly";
    }

    @GetMapping("/seller/analytics/products")
    public String showProductAnalytics(Model model, @AuthenticationPrincipal User userDetails) {
        model.addAttribute("topProducts", analyticsService.getTopProductSales(userDetails.getId()));
        return "seller/analytics-products";
    }

    @GetMapping("/seller/analytics/categories")
    public String showCategoryAnalytics(Model model, @AuthenticationPrincipal User userDetails) {
        model.addAttribute("categorySales", analyticsService.getCategorySales(userDetails.getId()));
        return "seller/analytics-categories";
    }

    @GetMapping("/seller/analytics/customers")
    public String showCustomerAnalytics(Model model, @AuthenticationPrincipal User userDetails) {
        model.addAttribute("customerActivity", analyticsService.getCustomerActivity(userDetails.getId()));
        return "seller/analytics-customers";
    }

    @GetMapping("/seller/analytics/sellers")
    public String showSellerComparison(Model model) {
        model.addAttribute("sellerComparison", analyticsService.getSellerComparison());
        return "seller/analytics-sellers";
    }
}