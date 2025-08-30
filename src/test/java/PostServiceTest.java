import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;
import ru.yandex.practicum.catsgram.service.PostService;
import ru.yandex.practicum.catsgram.service.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class PostServiceTest {

    private PostService postService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        postService = new PostService(userService);
    }

    @Test
    void create_ShouldCreatePost_WhenValidData() {
        User user = createTestUser("author@mail.ru");

        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setDescription("Нормальное описание");

        Post created = postService.create(post);

        assertNotNull(created.getId());
        assertNotNull(created.getPostDate());
        assertEquals("Нормальное описание", created.getDescription());
    }

    @Test
    void findAll_ShouldReturnSortedPosts() throws InterruptedException {
        User user = createTestUser("test@mail.ru");

        createTestPost(1L, user.getId(), "Первый", Instant.now().minusSeconds(259200));
        Thread.sleep(100);
        createTestPost(2L, user.getId(), "Второй", Instant.now().minusSeconds(172800));
        Thread.sleep(100);
        createTestPost(3L, user.getId(), "Третий", Instant.now());

        Collection<Post> result = postService.findAll(0, 10, "desc");
        assertEquals(3, result.size());
        assertEquals("Третий", result.iterator().next().getDescription());

        result = postService.findAll(0, 10, "asc");
        assertEquals("Первый", result.iterator().next().getDescription());
    }

    @Test
    void findAll_ShouldReturnLimitedPosts_WhenSizeSpecified() {
        User user = createTestUser("test@mail.ru");

        // Создаем 5 постов
        for (int i = 1; i <= 5; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        // Запрашиваем только 2 поста
        Collection<Post> result = postService.findAll(0, 2, "desc");

        assertEquals(2, result.size(), "Должно вернуть 2 поста");
    }

    @Test
    void findAll_ShouldReturnAllPosts_WhenSizeLargerThanTotal() {
        User user = createTestUser("test@mail.ru");

        // Создаем 3 поста
        for (int i = 1; i <= 3; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        // Запрашиваем 10 постов
        Collection<Post> result = postService.findAll(0, 10, "desc");

        assertEquals(3, result.size(), "Должно вернуть все 3 поста");
    }

    @Test
    void findAll_ShouldReturnCorrectSlice_WhenFromAndSizeSpecified() {
        User user = createTestUser("test@mail.ru");

        // Создаем 5 постов
        for (int i = 1; i <= 5; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        // Пропускаем 2 поста, берем 2 следующих
        Collection<Post> result = postService.findAll(2, 2, "desc");

        // Должны вернуться посты с 3 по 4 (после пропуска первых 2)
        assertEquals(2, result.size(), "Должно вернуть 2 поста");
    }

    @Test
    void findAll_ShouldReturnEmpty_WhenFromExceedsTotal() {
        User user = createTestUser("test@mail.ru");

        // Создаем 3 поста
        for (int i = 1; i <= 3; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        // Пытаемся взять с 10-й позиции
        Collection<Post> result = postService.findAll(10, 5, "desc");

        assertTrue(result.isEmpty(), "Должен вернуть пустой список");
    }

    @Test
    void findAll_ShouldReturnEmpty_WhenSizeZero() {
        User user = createTestUser("test@mail.ru");

        // Создаем 3 поста
        for (int i = 1; i <= 3; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        // Запрашиваем 0 постов
        Collection<Post> result = postService.findAll(0, 0, "desc");

        assertTrue(result.isEmpty(), "Должен вернуть пустой список при size=0");
    }

    @Test
    void findPostById_ShouldReturnPost_WhenExists() {
        User user = createTestUser("test@mail.ru");
        createTestPost(1L, user.getId(), "Test", Instant.now());

        Post found = postService.findPostById(1L);
        assertEquals("Test", found.getDescription());
    }

    @Test
    void findAll_ShouldReturnCorrectPosts_WhenPaginationUsed() throws InterruptedException {
        User user = createTestUser("test@mail.ru");

        // Создаем посты с разными описаниями. Нужен делей, тесты слишком быстро генерят данные и валятся
        createTestPost(1L, user.getId(), "Post A", Instant.now().minusSeconds(400));
        Thread.sleep(100);
        createTestPost(2L, user.getId(), "Post B", Instant.now().minusSeconds(300));
        Thread.sleep(100);
        createTestPost(3L, user.getId(), "Post C", Instant.now().minusSeconds(200));
        Thread.sleep(100);
        createTestPost(4L, user.getId(), "Post D", Instant.now().minusSeconds(100));
        Thread.sleep(100);

        // Берем 2 поста начиная со 2-й позиции
        Collection<Post> result = postService.findAll(2, 2, "desc");

        assertEquals(2, result.size());
        // Порядок: D, C, B, A
        // Пропускаем 2 (D, C), берем 2 следующих (B, A)
        List<Post> resultList = new ArrayList<>(result);
        assertEquals("Post B", resultList.get(0).getDescription());
        assertEquals("Post A", resultList.get(1).getDescription());
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        return userService.create(user);
    }

    private void createTestPost(Long id, Long authorId, String description, Instant date) {
        Post post = new Post();
        post.setId(id);
        post.setAuthorId(authorId);
        post.setDescription(description);
        post.setPostDate(date);
        postService.create(post);
    }
}
