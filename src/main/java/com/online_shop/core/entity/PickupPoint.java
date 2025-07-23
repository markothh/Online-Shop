package com.online_shop.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pickup_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickupPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
}