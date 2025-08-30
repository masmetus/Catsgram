import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;
import ru.yandex.practicum.catsgram.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void create_ShouldCreateUser_WhenValidData() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setPassword("password");

        User created = userService.create(user);

        assertNotNull(created.getId());
        assertNotNull(created.getRegistrationDate());
        assertEquals("test@mail.ru", created.getEmail());
    }

    @Test
    void create_ShouldThrowException_WhenEmailNull() {
        User user = new User();
        user.setPassword("password");

        assertThrows(ConditionsNotMetException.class, () -> userService.create(user));
    }

    @Test
    void create_ShouldThrowException_WhenEmailDuplicate() {
        User user1 = new User();
        user1.setEmail("duplicate@mail.ru");
        userService.create(user1);

        User user2 = new User();
        user2.setEmail("duplicate@mail.ru");

        assertThrows(DuplicatedDataException.class, () -> userService.create(user2));
    }

    @Test
    void findUserById_ShouldReturnUser_WhenExists() {
        User user = new User();
        user.setEmail("test@mail.ru");
        User created = userService.create(user);

        User found = userService.findUserById(created.getId());
        assertEquals("test@mail.ru", found.getEmail());
    }

    @Test
    void findUserById_ShouldThrowException_WhenNotExists() {
        assertThrows(NotFoundException.class, () -> userService.findUserById(999L));
    }
}