package stasiek.wojcik.wordletrainingproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.GuessResponse;
import stasiek.wojcik.wordletrainingproject.entity.Role;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuessServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private WordGenerator wordGenerator;


    private static List<Character> alphabet;
    private final static Character[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClassLoader classLoader = GuessServiceTest.class.getClassLoader();
    private GuessService service;

    @BeforeAll
    static void init() {
        alphabet = Arrays.asList(letters);
    }

    @BeforeEach
    void reset() {
        service = new GuessService(repository, wordGenerator);
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return alphabet.stream().collect(Collectors.toMap(value -> value, value -> LetterResult.NOT_USED));
    }

    private GuessResponse importGuessResponse(final String filename) throws IOException {
        final var fileURL = classLoader.getResource("GuessServiceTest/" + filename + ".json");
        final var file = new File(Objects.requireNonNull(fileURL).getFile());
        return objectMapper.readValue(file, GuessResponse.class);
    }

    @Test
    void shouldReturnProperResponseForCorrectWord() throws IOException {
        final var guess = "guess";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game(guess, "", 0, generateKeyboardMap(), SessionStatus.IN_PROGRESS);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(true);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var actualResponse = service.processGuess("username", guess);
        final var expectedResponse = importGuessResponse("CorrectGuessWithWinStatus");

        assertFalse(actualResponse.isEmpty());
        assertEquals(expectedResponse, actualResponse.get());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(1)).save(user);
    }

    @Test
    void shouldReturnProperResponseForIncorrectWordWithNoCorrectLetters() throws IOException {
        final var guess = "incor";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game("guess", "", 0, generateKeyboardMap(), SessionStatus.IN_PROGRESS);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(true);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var actualResponse = service.processGuess("username", guess);
        final var expectedResponse = importGuessResponse("IncorrectGuessWithNoCorrectLetters");

        assertFalse(actualResponse.isEmpty());
        assertEquals(expectedResponse, actualResponse.get());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(1)).save(user);
    }

    @Test
    void shouldReturnProperResponseForIncorrectWordWithCorrectLetters() throws IOException {
        final var guess = "gxxxs";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game("guess", "", 0, generateKeyboardMap(), SessionStatus.IN_PROGRESS);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(true);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var actualResponse = service.processGuess("username", guess);
        final var expectedResponse = importGuessResponse("IncorrectGuessWithCorrectLetters");

        assertFalse(actualResponse.isEmpty());
        assertEquals(expectedResponse, actualResponse.get());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(1)).save(user);
    }

    @Test
    void shouldReturnEmptyOptionalForInvalidWord() {
        final var guess = "inval";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game("guess", "", 0, generateKeyboardMap(), SessionStatus.IN_PROGRESS);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(false);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var guessResponse = service.processGuess("username", guess);

        assertTrue(guessResponse.isEmpty());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(0)).save(user);
    }

    @Test
    void shouldReturnProperResponseForIncorrectWordWithCorrectLettersForInvalidGameStatus() throws IOException {
        final var guess = "gxxxs";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game("guess", guess, 6, generateKeyboardMap(), SessionStatus.FAILED);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(true);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var actualResponse = service.processGuess("username", guess);
        final var expectedResponse = importGuessResponse("CorrectGuessWithInvalidGameStatus");

        assertFalse(actualResponse.isEmpty());
        assertEquals(expectedResponse, actualResponse.get());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(0)).save(user);
    }


    @Test
    void shouldReturnProperResponseForCorrectWordWithMisplacedLetters() throws IOException {
        final var guess = "xssgx";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game("guess", guess, 0, generateKeyboardMap(), SessionStatus.IN_PROGRESS);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(true);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var actualResponse = service.processGuess("username", guess);
        final var expectedResponse = importGuessResponse("CorrectGuessWithMisplacedLetters");

        assertFalse(actualResponse.isEmpty());
        assertEquals(expectedResponse, actualResponse.get());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(1)).save(user);
    }

    @Test
    void shouldReturnProperResponseForCorrectWordWithLetterUsedMultipleTimesInCorrectAndMisplacedPosition() throws IOException {
        final var guess = "axaaa";
        final var user = new User("username", "password", Role.USER);
        final var game = new Game("xaxax", guess, 0, generateKeyboardMap(), SessionStatus.IN_PROGRESS);
        user.setGame(game);

        when(wordGenerator.isOnWordList(guess)).thenReturn(true);
        when(repository.findUserByUsername("username")).thenReturn(Optional.of(user));
        final var actualResponse = service.processGuess("username", guess);
        final var expectedResponse = importGuessResponse("CorrectGuessContainingCorrectAndMisplacedLetters");

        assertFalse(actualResponse.isEmpty());
        assertEquals(expectedResponse, actualResponse.get());
        verify(repository, times(1)).findUserByUsername("username");
        verify(repository, times(1)).save(user);
    }
}
