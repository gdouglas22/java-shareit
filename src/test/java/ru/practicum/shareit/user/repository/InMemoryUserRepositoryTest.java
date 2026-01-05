package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void save_assignsIdAndUpdatesExisting() {
        User user = User.builder().name("Name").email("mail@mail.com").build();

        User saved = repository.save(user);
        saved.setName("Updated");
        User updated = repository.save(saved);

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), updated.getId());
        assertEquals("Updated", repository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void findByEmail_whenEmailNull_returnsEmpty() {
        assertTrue(repository.findByEmail(null).isEmpty());
    }

    @Test
    void findByEmail_ignoresCase() {
        repository.save(User.builder().name("Name").email("mail@mail.com").build());

        Optional<User> found = repository.findByEmail("MAIL@mail.com");

        assertTrue(found.isPresent());
        assertEquals("mail@mail.com", found.orElseThrow().getEmail());
    }

    @Test
    void deleteById_removesUser() {
        User saved = repository.save(User.builder().name("Name").email("mail@mail.com").build());

        repository.deleteById(saved.getId());

        assertFalse(repository.findById(saved.getId()).isPresent());
    }
}
