
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.catsgram.service.PostService;
import ru.yandex.practicum.catsgram.service.UserService;
import ru.yandex.practicum.catsgram.util.SortOrder;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;

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

        Collection<Post> result = postService.findAll(0, 10, SortOrder.DESC);
        assertEquals(3, result.size());
        assertEquals("Третий", result.iterator().next().getDescription());

        result = postService.findAll(0, 10, SortOrder.ASC);
        assertEquals("Первый", result.iterator().next().getDescription());
    }

    @Test
    void findAll_ShouldReturnLimitedPosts_WhenSizeSpecified() {
        User user = createTestUser("test@mail.ru");

        for (int i = 1; i <= 5; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        Collection<Post> result = postService.findAll(0, 2, SortOrder.DESC);
        assertEquals(2, result.size(), "Должно вернуть 2 поста");
    }

    @Test
    void findAll_ShouldReturnAllPosts_WhenSizeLargerThanTotal() {
        User user = createTestUser("test@mail.ru");

        for (int i = 1; i <= 3; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        Collection<Post> result = postService.findAll(0, 10, SortOrder.DESC);
        assertEquals(3, result.size(), "Должно вернуть все 3 поста");
    }

    @Test
    void findAll_ShouldReturnCorrectSlice_WhenFromAndSizeSpecified() {
        User user = createTestUser("test@mail.ru");

        for (int i = 1; i <= 5; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        Collection<Post> result = postService.findAll(2, 2, SortOrder.DESC);
        assertEquals(2, result.size(), "Должно вернуть 2 поста");
    }

    @Test
    void findAll_ShouldReturnEmpty_WhenFromExceedsTotal() {
        User user = createTestUser("test@mail.ru");

        for (int i = 1; i <= 3; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        Collection<Post> result = postService.findAll(10, 5, SortOrder.DESC);
        assertTrue(result.isEmpty(), "Должен вернуть пустой список");
    }

    @Test
    void findAll_ShouldThrowException_WhenSizeZero() {
        User user = createTestUser("test@mail.ru");

        for (int i = 1; i <= 3; i++) {
            createTestPost((long) i, user.getId(), "Post " + i, Instant.now().minusSeconds(i * 100));
        }

        assertThrows(ParameterNotValidException.class, () -> {
            postService.findAll(0, 0, SortOrder.DESC);
        });
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

        createTestPost(1L, user.getId(), "Post A", Instant.now().minusSeconds(400));
        Thread.sleep(100);
        createTestPost(2L, user.getId(), "Post B", Instant.now().minusSeconds(300));
        Thread.sleep(100);
        createTestPost(3L, user.getId(), "Post C", Instant.now().minusSeconds(200));
        Thread.sleep(100);
        createTestPost(4L, user.getId(), "Post D", Instant.now().minusSeconds(100));
        Thread.sleep(100);

        Collection<Post> result = postService.findAll(2, 2, SortOrder.DESC);
        assertEquals(2, result.size());

        List<Post> resultList = new ArrayList<>(result);
        assertEquals("Post B", resultList.get(0).getDescription());
        assertEquals("Post A", resultList.get(1).getDescription());
    }

    @Test
    void findAll_ShouldThrowException_WhenFromNegative() {
        assertThrows(ParameterNotValidException.class, () -> {
            postService.findAll(-1, 10, SortOrder.DESC);
        });
    }

    @Test
    void findAll_ShouldThrowException_WhenSizeNegative() {
        assertThrows(ParameterNotValidException.class, () -> {
            postService.findAll(0, -5, SortOrder.DESC);
        });
    }

    @Test
    void findPostById_ShouldThrowNotFoundException_WhenNotExists() {
        assertThrows(NotFoundException.class, () -> {
            postService.findPostById(999L);
        });
    }

    @Test
    void create_ShouldThrowConditionsNotMetException_WhenDescriptionNull() {
        User user = createTestUser("test@mail.ru");

        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setDescription(null);

        assertThrows(ConditionsNotMetException.class, () -> {
            postService.create(post);
        });
    }

    @Test
    void create_ShouldThrowConditionsNotMetException_WhenDescriptionBlank() {
        User user = createTestUser("test@mail.ru");

        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setDescription("   ");

        assertThrows(ConditionsNotMetException.class, () -> {
            postService.create(post);
        });
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenPostNotExists() {
        User user = createTestUser("test@mail.ru");

        Post post = new Post();
        post.setId(999L);
        post.setAuthorId(user.getId());
        post.setDescription("Valid description");

        assertThrows(NotFoundException.class, () -> {
            postService.update(post);
        });
    }

    @Test
    void update_ShouldThrowConditionsNotMetException_WhenIdNull() {
        User user = createTestUser("test@mail.ru");

        Post post = new Post();
        post.setId(null);
        post.setAuthorId(user.getId());
        post.setDescription("Valid description");

        assertThrows(ConditionsNotMetException.class, () -> {
            postService.update(post);
        });
    }

    @Test
    void update_ShouldThrowConditionsNotMetException_WhenDescriptionBlank() {
        User user = createTestUser("test@mail.ru");
        createTestPost(1L, user.getId(), "Original", Instant.now());

        Post post = new Post();
        post.setId(1L);
        post.setAuthorId(user.getId());
        post.setDescription("   ");

        assertThrows(ConditionsNotMetException.class, () -> {
            postService.update(post);
        });
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