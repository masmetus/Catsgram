package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> existingEmails = new HashSet<>();

    @GetMapping
    public Collection<User> allUser() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Email должен быть указан");
        }

        if (existingEmails.contains(user.getEmail())) {
            throw new DuplicatedDataException("Этот email уже используется");
        }

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        existingEmails.add(user.getEmail());

        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан.");
        }
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getUsername() != null && !newUser.getUsername().isBlank()) {
            oldUser.setUsername(newUser.getUsername());
        }
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()
                && !newUser.getEmail().equals(oldUser.getEmail())) {

            if (existingEmails.contains(newUser.getEmail())) {
                throw new DuplicatedDataException("Этот email уже используется.");
            }

            existingEmails.remove(oldUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            existingEmails.add(newUser.getEmail());
        }
        if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
            oldUser.setPassword(newUser.getPassword());
        }

        return oldUser;
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
