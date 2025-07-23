package com.online_shop.core.repository;

import com.online_shop.core.entity.UserCard;
import com.online_shop.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    List<UserCard> findByUser(User user);
}