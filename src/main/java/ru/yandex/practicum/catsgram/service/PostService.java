package ru.yandex.practicum.catsgram.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class PostService {

    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    private final Map<Long, Post> posts = new HashMap<>();

    public Collection<Post> findAll(int from, int size, String sort) {
        if (!List.of("asc", "desc").contains(sort.toLowerCase())) {
            throw new ValidationException("Параметр 'sort' должен быть равен 'asc' или 'desc'");
        }
        if (from < 0) {
            throw new ConditionsNotMetException("Параметр 'from' не может быть отрицательным");
        }
        if (size < 0) {
            throw new ConditionsNotMetException("Параметр 'size' не может быть отрицательным");
        }

        Comparator<Post> comparator = Comparator.comparing(Post::getPostDate);
        if ("desc".equalsIgnoreCase(sort)) {
            comparator = comparator.reversed();
        }

        return posts.values().stream()
                .sorted(comparator)
                .skip(from)
                .limit(size)
                .toList();
    }

    public Post findPostById(Long id) {
        Post post = findPostByIdOptional(id)
                .orElseThrow(() -> {
                    log.warn("Пост с id={} не найден", id);
                    return new NotFoundException("Пост с id= " + id + " не найден");
                });
        log.info("Найден пост: id={}", post.getId());
        return post;
    }

    public Optional<Post> findPostByIdOptional(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(posts.get(id));
    }

    // Не использую Optional, так как тут в нём нет смысла. Мы сразу же проверяем на наличие
    // Только усложню синтаксис, где это не нужно
    public Post create(Post post) {
        userService.findUserById(post.getAuthorId());
        validateCreatePost(post);

        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    // TODO: добавить проверку существования автора поста
    public Post update(Post newPost) {
        // проверяем необходимые условия
        userService.findUserById(newPost.getAuthorId());
        validateUpdatePost(newPost);

        Post oldPost = posts.get(newPost.getId());
        // если публикация найдена и все условия соблюдены, обновляем её содержимое
        oldPost.setDescription(newPost.getDescription());
        return oldPost;
    }

    private void validateCreatePost(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
    }

    private void validateUpdatePost(Post post) {
        if (post.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан.");
        }
        if (!posts.containsKey(post.getId())) {
            throw new NotFoundException("Пост с id = " + post.getId() + " не найден.");
        }
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым.");
        }
    }


    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
