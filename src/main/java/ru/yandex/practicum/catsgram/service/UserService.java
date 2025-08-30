package ru.yandex.practicum.catsgram.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> existingEmails = new HashSet<>();


    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public User findUserById(Long authorId) {
        User user = findUserByIdOptional(authorId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", authorId);
                    return new NotFoundException("Пользователь с id=" + authorId + " не найден.");
                });
        log.info("Найден пользователь: id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    public Optional<User> findUserByIdOptional(Long authorId) {
        if (authorId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(authorId));
    }

    public User create(@Valid User user) {
        log.info("POST /users - Создание пользователя: {}", user.getEmail());
        validateEmail(user);

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        existingEmails.add(user.getEmail());

        log.info("Пользователь создан успешно: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }


    public User update(@Valid User newUser) {
        log.info("PUT /users - Обновление пользователя: ID={}", newUser.getId());
        validateExistingUser(newUser);


        User oldUser = users.get(newUser.getId());

        if (newUser.getUsername() != null && !newUser.getUsername().isBlank()) {
            oldUser.setUsername(newUser.getUsername());
        }
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()
                && !newUser.getEmail().equals(oldUser.getEmail())) {

            validateEmail(newUser);

            existingEmails.remove(oldUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            existingEmails.add(newUser.getEmail());
        }
        if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
            oldUser.setPassword(newUser.getPassword());
        }

        log.info("Пользователь обновлен успешно: ID={}", oldUser.getId());
        return oldUser;
    }

    private void validateEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации: email не указан");
            throw new ConditionsNotMetException("Email должен быть указан.");
        }

        if (existingEmails.contains(user.getEmail())) {
            log.warn("Ошибка валидации: email уже используется");
            throw new DuplicatedDataException("Этот email уже используется.");
        }
    }

    private void validateExistingUser(User user) {
        if (user.getId() == null) {
            log.warn("Ошибка валидации: ID не указан");
            throw new ConditionsNotMetException("Id должен быть указан.");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("Ошибка: Пользователь с ID={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден.");
        }

    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
