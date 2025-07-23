package com.online_shop.core.service;

import com.online_shop.core.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private OrderItemRepository orderItemRepository;

    // Продажи по товарам
    public List<ProductSales> getProductSales(Long sellerId) {
        return orderItemRepository.findProductSalesBySeller(sellerId);
    }

    // Динамика продаж по месяцам
    public List<MonthlySales> getMonthlySales(Long sellerId) {
        return orderItemRepository.findMonthlySalesBySeller(sellerId);
    }

    // Топ товаров
    public List<TopProductSales> getTopProductSales(Long sellerId) {
        List<TopProductSales> sales = orderItemRepository.findTopProductSalesBySeller(sellerId);
        Double totalRevenue = sales.stream()
                .mapToDouble(TopProductSales::getTotalRevenue)
                .sum();
        return sales.stream()
                .map(sale -> new TopProductSales(
                        sale.getProductTitle(),
                        sale.getTotalQuantity(),
                        sale.getTotalRevenue(),
                        totalRevenue == 0 ? 0.0 : (sale.getTotalRevenue() / totalRevenue * 100)
                ))
                .collect(Collectors.toList());
    }

    // Продажи по категориям
    public List<CategorySales> getCategorySales(Long sellerId) {
        List<CategorySales> sales = orderItemRepository.findCategorySalesBySeller(sellerId);
        final int[] rank = {1};
        AtomicReference<Double> prevRevenue = new AtomicReference<>();
        return sales.stream().map(sale -> {
            int currentRank = sale.getTotalRevenue().equals(prevRevenue) ? rank[0] : rank[0]++;
            prevRevenue.set(sale.getTotalRevenue());
            return new CategorySales(
                    sale.getCategory(),
                    sale.getTotalItems(),
                    sale.getTotalQuantity(),
                    sale.getTotalRevenue(),
                    currentRank
            );
        }).collect(Collectors.toList());
    }

    // Активность покупателей
    public List<CustomerActivity> getCustomerActivity(Long sellerId) {
        return orderItemRepository.findCustomerActivityBySeller(sellerId);
    }

    // Сравнение продавцов
    public List<SellerComparison> getSellerComparison() {
        List<SellerComparison> comparisons = orderItemRepository.findSellerComparison();
        final int[] rank = {1};
        AtomicReference<Double> prevRevenue = new AtomicReference<>();
        return comparisons.stream().map(comparison -> {
            int currentRank = comparison.getTotalRevenue().equals(prevRevenue) ? rank[0] : rank[0]++;
            prevRevenue.set(comparison.getTotalRevenue());
            return new SellerComparison(
                    comparison.getUsername(),
                    comparison.getTotalProducts(),
                    comparison.getTotalQuantitySold(),
                    comparison.getTotalRevenue(),
                    comparison.getAvgRevenuePerProduct(),
                    currentRank
            );
        }).collect(Collectors.toList());
    }

    // DTO для продаж по товарам
    public static class ProductSales {
        private String productTitle;
        private Long totalQuantity;
        private Double totalRevenue;

        public ProductSales(String productTitle, Long totalQuantity, Double totalRevenue) {
            this.productTitle = productTitle;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
        }

        public String getProductTitle() { return productTitle; }
        public Long getTotalQuantity() { return totalQuantity; }
        public Double getTotalRevenue() { return totalRevenue; }
    }

    // DTO для месячных продаж
    public static class MonthlySales {
        private Integer saleYear;
        private Integer saleMonth;
        private Long totalItems;
        private Double totalRevenue;
        private String monthName;

        public MonthlySales(Integer saleYear, Integer saleMonth, Long totalItems, Double totalRevenue) {
            this.saleYear = saleYear;
            this.saleMonth = saleMonth;
            this.totalItems = totalItems;
            this.totalRevenue = totalRevenue;
            this.monthName = Month.of(saleMonth).name().substring(0, 3) + " " + saleYear;
        }

        public Integer getSaleYear() { return saleYear; }
        public Integer getSaleMonth() { return saleMonth; }
        public Long getTotalItems() { return totalItems; }
        public Double getTotalRevenue() { return totalRevenue; }
        public String getMonthName() { return monthName; }
    }

    // DTO для топ товаров
    public static class TopProductSales {
        private String productTitle;
        private Long totalQuantity;
        private Double totalRevenue;
        private Double revenueShare;

        public TopProductSales(String productTitle, Long totalQuantity, Double totalRevenue, Double revenueShare) {
            this.productTitle = productTitle;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
            this.revenueShare = revenueShare;
        }

        public String getProductTitle() { return productTitle; }
        public Long getTotalQuantity() { return totalQuantity; }
        public Double getTotalRevenue() { return totalRevenue; }
        public Double getRevenueShare() { return revenueShare; }
    }

    // DTO для продаж по категориям
    public static class CategorySales {
        private String category;
        private Long totalItems;
        private Long totalQuantity;
        private Double totalRevenue;
        private Integer revenueRank;

        public CategorySales(String category, Long totalItems, Long totalQuantity, Double totalRevenue, Integer revenueRank) {
            this.category = category;
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
            this.revenueRank = revenueRank;
        }

        public String getCategory() { return category; }
        public Long getTotalItems() { return totalItems; }
        public Long getTotalQuantity() { return totalQuantity; }
        public Double getTotalRevenue() { return totalRevenue; }
        public Integer getRevenueRank() { return revenueRank; }
    }

    // DTO для активности покупателей
    public static class CustomerActivity {
        private Integer saleYear;
        private Integer saleMonth;
        private Long uniqueCustomers;
        private Long repeatCustomers;
        private String monthName;

        public CustomerActivity(Integer saleYear, Integer saleMonth, Long uniqueCustomers, Long repeatCustomers) {
            this.saleYear = saleYear;
            this.saleMonth = saleMonth;
            this.uniqueCustomers = uniqueCustomers;
            this.repeatCustomers = repeatCustomers;
            this.monthName = Month.of(saleMonth).name().substring(0, 3) + " " + saleYear;
        }

        public Integer getSaleYear() { return saleYear; }
        public Integer getSaleMonth() { return saleMonth; }
        public Long getUniqueCustomers() { return uniqueCustomers; }
        public Long getRepeatCustomers() { return repeatCustomers; }
        public String getMonthName() { return monthName; }
    }

    // DTO для сравнения продавцов
    public static class SellerComparison {
        private String username;
        private Long totalProducts;
        private Long totalQuantitySold;
        private Double totalRevenue;
        private Double avgRevenuePerProduct;
        private Integer revenueRank;

        public SellerComparison(String username, Long totalProducts, Long totalQuantitySold, Double totalRevenue,
                                Double avgRevenuePerProduct, Integer revenueRank) {
            this.username = username;
            this.totalProducts = totalProducts;
            this.totalQuantitySold = totalQuantitySold;
            this.totalRevenue = totalRevenue;
            this.avgRevenuePerProduct = avgRevenuePerProduct;
            this.revenueRank = revenueRank;
        }

        public String getUsername() { return username; }
        public Long getTotalProducts() { return totalProducts; }
        public Long getTotalQuantitySold() { return totalQuantitySold; }
        public Double getTotalRevenue() { return totalRevenue; }
        public Double getAvgRevenuePerProduct() { return avgRevenuePerProduct; }
        public Integer getRevenueRank() { return revenueRank; }
    }
}