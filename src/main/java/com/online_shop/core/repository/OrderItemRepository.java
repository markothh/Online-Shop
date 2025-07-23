package com.online_shop.core.repository;

import com.online_shop.core.entity.OrderItem;
import com.online_shop.core.service.AnalyticsService.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Продажи по товарам
    @Query("SELECT new com.online_shop.core.service.AnalyticsService$ProductSales(" +
            "p.title, SUM(oi.quantity), SUM(oi.quantity * p.price)) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN oi.order o " +
            "WHERE p.seller.id = :sellerId AND o.isPaid = true " +
            "GROUP BY p.title")
    List<ProductSales> findProductSalesBySeller(Long sellerId);

    // Динамика продаж по месяцам
    @Query("SELECT new com.online_shop.core.service.AnalyticsService$MonthlySales(" +
            "YEAR(o.orderDate), MONTH(o.orderDate), " +
            "COUNT(DISTINCT oi.id), SUM(oi.quantity * p.price)) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN oi.order o " +
            "WHERE p.seller.id = :sellerId AND o.isPaid = true " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<MonthlySales> findMonthlySalesBySeller(Long sellerId);

    // Топ товаров
    @Query("SELECT new com.online_shop.core.service.AnalyticsService$TopProductSales(" +
            "p.title, SUM(oi.quantity), SUM(oi.quantity * p.price), 0.0) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN oi.order o " +
            "WHERE p.seller.id = :sellerId AND o.isPaid = true " +
            "GROUP BY p.title " +
            "ORDER BY SUM(oi.quantity * p.price) DESC")
    List<TopProductSales> findTopProductSalesBySeller(Long sellerId);

    // Продажи по категориям
    @Query("SELECT new com.online_shop.core.service.AnalyticsService$CategorySales(" +
            "p.category, COUNT(DISTINCT oi.id), SUM(oi.quantity), SUM(oi.quantity * p.price), 0) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN oi.order o " +
            "WHERE p.seller.id = :sellerId AND o.isPaid = true " +
            "GROUP BY p.category " +
            "ORDER BY SUM(oi.quantity * p.price) DESC")
    List<CategorySales> findCategorySalesBySeller(Long sellerId);

    // Активность покупателей
    @Query("SELECT new com.online_shop.core.service.AnalyticsService$CustomerActivity(" +
            "YEAR(o.orderDate), MONTH(o.orderDate), " +
            "COUNT(DISTINCT o.user.id), " +
            "COUNT(DISTINCT CASE WHEN (" +
            "SELECT COUNT(DISTINCT o2.id) " +
            "FROM Order o2 " +
            "WHERE o2.user.id = o.user.id AND o2.isPaid = true" +
            ") > 1 THEN o.user.id END)) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.product p " +
            "WHERE p.seller.id = :sellerId AND o.isPaid = true " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<CustomerActivity> findCustomerActivityBySeller(Long sellerId);

    // Сравнение продавцов
    @Query("SELECT new com.online_shop.core.service.AnalyticsService$SellerComparison(" +
            "u.username, COUNT(DISTINCT p.id), COALESCE(SUM(oi.quantity), 0), " +
            "COALESCE(SUM(oi.quantity * p.price), 0), " +
            "COALESCE(SUM(oi.quantity * p.price) / NULLIF(COUNT(DISTINCT p.id), 0), 0), 0) " +
            "FROM User u " +
            "LEFT JOIN Product p ON p.seller.id = u.id " +
            "LEFT JOIN OrderItem oi ON oi.product.id = p.id " +
            "LEFT JOIN Order o ON oi.order.id = o.id AND o.isPaid = true " +
            "WHERE u.isSeller = true " +
            "GROUP BY u.username " +
            "ORDER BY COALESCE(SUM(oi.quantity * p.price), 0) DESC")
    List<SellerComparison> findSellerComparison();
}