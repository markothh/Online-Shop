package com.online_shop.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private double price;
    private String category;
    private String image;
    private int stock;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
}
