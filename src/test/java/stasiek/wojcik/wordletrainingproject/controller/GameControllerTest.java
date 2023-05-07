package stasiek.wojcik.wordletrainingproject.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.Role;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.security.Principal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus.IN_PROGRESS;

@AutoConfigureMockMvc
class GameControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        insertUser();
    }

    @AfterEach
    void reset() {
        userRepository.deleteAll();
    }


    private void insertUser() {
        var user = new User("user", "password", Role.USER);
        final var game = new Game("guess", null, 0, Collections.emptyMap(), IN_PROGRESS);
        user.setGame(game);
        userRepository.save(user);
    }

    @Test
    @WithMockUser
    void shouldReturn200StatusAndStartNewGameForValidUser() throws Exception {
        final var principal = mock(Principal.class);
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/start-game")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("New game started.", mvcResult.getResponse().getContentAsString());

        final var user = userRepository.findUserByUsername("user");
        final var game = user.map(User::getGame).orElseThrow();
        assertEquals(0, game.getAttemptsCounter());
        assertEquals(IN_PROGRESS, game.getStatus());
    }

    @Test
    void shouldReturn401StatusForUnauthorizedUser() throws Exception {
        final var principal = mock(Principal.class);
        mockMvc.perform(MockMvcRequestBuilders.post("/start-game")
                        .principal(principal))
                .andExpect(status().isUnauthorized());
    }
}