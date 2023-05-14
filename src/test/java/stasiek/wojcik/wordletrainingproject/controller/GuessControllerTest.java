package stasiek.wojcik.wordletrainingproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import stasiek.wojcik.wordletrainingproject.entity.*;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static stasiek.wojcik.wordletrainingproject.entity.result.LetterResult.NOT_USED;
import static stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus.*;

@AutoConfigureMockMvc
class GuessControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Value("${alphabet}")
    private List<Character> ALPHABET;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User user;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @AfterEach
    void reset() {
        userRepository.delete(user);
    }

    private void insertUser(final int guessAttempts) {
        user = new User("user", "password", Role.USER);
        final var game = new Game("guess", null, guessAttempts, generateKeyboardMap(), IN_PROGRESS);
        user.setGame(game);
        userRepository.save(user);
    }

    private void insertUserWithoutGame() {
        user = new User("user", "password", Role.USER);
        userRepository.save(user);
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return ALPHABET.stream().collect(Collectors.toMap(value -> value, value -> NOT_USED));
    }

    @Test
    void shouldReturn401CodeForInvalidTokenWhenCorrectContentProvided() throws Exception {
        insertUser(0);
        final var guessRequest = new GuessRequest("guess");
        final var principal = mock(Principal.class);
        mockMvc.perform(MockMvcRequestBuilders.post("/guess")
                        .content(objectMapper.writeValueAsString(guessRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    @WithMockUser
    void shouldReturn200CodeAndGuessResponseWithWINStatusForCorrectGuess() throws Exception {
        insertUser(0);
        final var guessRequest = new GuessRequest("guess");
        final var principal = mock(Principal.class);

        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/guess")
                        .content(objectMapper.writeValueAsString(guessRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        final var guessResponse =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GuessResponse.class);
        assertThat(guessResponse).isNotNull();
        assertEquals(1, guessResponse.attempts());
        assertEquals(WIN, guessResponse.status());

        final var user = userRepository.findUserByUsername("user");
        final var game = user.map(User::getGame).orElseThrow();
        assertEquals(1, game.getAttemptsCounter());
        assertEquals(WIN, game.getStatus());
    }

    @Test
    @WithMockUser
    void shouldReturn200CodeAndGuessResponseWithIN_PROGRESSStatusForIncorrectGuess() throws Exception {
        insertUser(3);
        final var guessRequest = new GuessRequest("apple");
        final var principal = mock(Principal.class);

        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/guess")
                        .content(objectMapper.writeValueAsString(guessRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        final var guessResponse =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GuessResponse.class);
        assertThat(guessResponse).isNotNull();
        assertEquals(4, guessResponse.attempts());
        assertEquals(IN_PROGRESS, guessResponse.status());

        final var user = userRepository.findUserByUsername("user");
        final var game = user.map(User::getGame).orElseThrow();
        assertEquals(4, game.getAttemptsCounter());
        assertEquals(IN_PROGRESS, game.getStatus());
    }

    @Test
    @WithMockUser
    void shouldReturn200CodeAndGuessResponseWithFAILEDStatusForLastIncorrectGuess() throws Exception {
        insertUser(5);
        final var guessRequest = new GuessRequest("apple");
        final var principal = mock(Principal.class);

        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/guess")
                        .content(objectMapper.writeValueAsString(guessRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        final var guessResponse =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GuessResponse.class);
        assertThat(guessResponse).isNotNull();
        assertEquals(6, guessResponse.attempts());
        assertEquals(FAILED, guessResponse.status());

        final var user = userRepository.findUserByUsername("user");
        final var game = user.map(User::getGame).orElseThrow();
        assertEquals(6, game.getAttemptsCounter());
    }

    @Test
    @WithMockUser
    void shouldReturn404ForUserWithoutGame() throws Exception {
        insertUserWithoutGame();
        final var guessRequest = new GuessRequest("apple");
        final var principal = mock(Principal.class);

        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/guess")
                        .content(objectMapper.writeValueAsString(guessRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal))
                .andExpect(status().is(404))
                .andReturn();

        assertEquals("User haven't started any games yet.", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    void shouldReturn406ForInvalidInputProvided() throws Exception {
        insertUserWithoutGame();
        final var guessRequest = new GuessRequest("11aa1");
        final var principal = mock(Principal.class);

        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/guess")
                        .content(objectMapper.writeValueAsString(guessRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal))
                .andExpect(status().is(406))
                .andReturn();

        assertEquals("Invalid input provided.", mvcResult.getResponse().getContentAsString());
    }
}