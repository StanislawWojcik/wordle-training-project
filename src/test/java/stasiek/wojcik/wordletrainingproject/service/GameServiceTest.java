package stasiek.wojcik.wordletrainingproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stasiek.wojcik.wordletrainingproject.entity.Role;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private WordGenerator wordGenerator;

    private GameService service;

    @BeforeEach
    void reset() {
        this.service = new GameService(repository, wordGenerator, Collections.emptySet());
    }

    @Test
    void shouldReturnNewGameWithCorrectInitialValues() {
        final var user = new User("username", "password", Role.USER);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        when(wordGenerator.generateWord()).thenReturn("guess");

        final var actualGame = service.startNewGame("username");

        assertTrue(actualGame.isPresent());
        assertEquals("guess", actualGame.get().getWord());
        assertEquals(SessionStatus.IN_PROGRESS, actualGame.get().getStatus());
        assertEquals(0, actualGame.get().getAttemptsCounter());
        assertNull(actualGame.get().getLastGuess());
        verify(repository, times(1)).save(user);
    }
}