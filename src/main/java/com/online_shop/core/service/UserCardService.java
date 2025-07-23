package com.online_shop.core.service;

import com.online_shop.core.entity.User;
import com.online_shop.core.entity.UserCard;
import com.online_shop.core.repository.UserCardRepository;
import com.online_shop.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCardService {
    private static final Logger logger = LoggerFactory.getLogger(UserCardService.class);
    private final UserCardRepository userCardRepository;
    private final UserRepository userRepository;

    public UserCardService(UserCardRepository userCardRepository, UserRepository userRepository) {
        this.userCardRepository = userCardRepository;
        this.userRepository = userRepository;
    }

    public List<UserCard> getUserCards() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return userCardRepository.findByUser(user);
    }

    public UserCard saveCard(String cardNumber, int expiryMonth, int expiryYear) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("Пользователь не найден: {}", username);
            throw new IllegalArgumentException("Пользователь не найден: " + username);
        }

        if (expiryMonth < 1 || expiryMonth > 12) {
            logger.error("Некорректный месяц: {}", expiryMonth);
            throw new IllegalArgumentException("Месяц должен быть от 1 до 12");
        }
        if (expiryYear < 2025) {
            logger.error("Некорректный год: {}", expiryYear);
            throw new IllegalArgumentException("Год должен быть не ранее 2025");
        }

        UserCard card = new UserCard();
        card.setUser(user);
        card.setCardToken(generateCardToken(cardNumber));
        card.setCardLast4(cardNumber.substring(cardNumber.length() - 4));
        card.setExpiryMonth(expiryMonth);
        card.setExpiryYear(expiryYear);
        card.setCreatedAt(LocalDateTime.now());

        logger.info("Сохранение карты для пользователя {}, последние 4 цифры: {}", username, card.getCardLast4());
        return userCardRepository.save(card);
    }

    private String generateCardToken(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cardNumber.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Ошибка генерации токена карты: {}", e.getMessage());
            throw new RuntimeException("Не удалось сгенерировать токен карты");
        }
    }
}