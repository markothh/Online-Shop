package com.online_shop.core.service;

import com.online_shop.core.entity.PickupPoint;
import com.online_shop.core.repository.PickupPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PickupPointService {
    private final PickupPointRepository pickupPointRepository;

    public PickupPointService(PickupPointRepository pickupPointRepository) {
        this.pickupPointRepository = pickupPointRepository;
    }

    public List<PickupPoint> getAllPickupPoints() {
        return pickupPointRepository.findAll();
    }
}