package stasiek.wojcik.wordletrainingproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import stasiek.wojcik.wordletrainingproject.entity.Role;
import stasiek.wojcik.wordletrainingproject.entity.Token;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.UserCredentialsForm;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthenticationControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        insertUser();
    }

    @AfterEach
    void reset() {
        userRepository.deleteAll();
    }

    private void insertUser() {
        final var user = new User("mockUser",
                new BCryptPasswordEncoder().encode("mockPassword"), Role.USER);
        userRepository.save(user);
    }

    @Test
    void shouldReturnStatus200AndCorrectMessageWhenValidUserProvidedForRegistration() throws Exception {
        final var userCredentials = new UserCredentialsForm("mockUser1", "mockPassword1");
        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .content(objectMapper.writeValueAsString(userCredentials))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        final var responseMessage = mvcResult.getResponse().getContentAsString();
        final var expectedResponseMessage = "User registered.";
        assertEquals(expectedResponseMessage, responseMessage);
    }

    @Test
    void shouldReturnStatus409AndCorrectMessageWhenUserAlreadyExistsInDatabase() throws Exception {
        final var userCredentials = new UserCredentialsForm("mockUser", "mockPassword");
        final var mvcResultDuplicate = mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .content(objectMapper.writeValueAsString(userCredentials))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();

        final var responseMessageDuplicate = mvcResultDuplicate.getResponse().getContentAsString();
        final var expectedResponseMessageDuplicate = "User already exists in database.";
        assertEquals(expectedResponseMessageDuplicate, responseMessageDuplicate);
    }

    @Test
    void shouldReturnStatus200AndValidTokenForCorrectUserCredentialsForm() throws Exception {
        final var userCredentials = new UserCredentialsForm("mockUser", "mockPassword");
        final var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(userCredentials))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final var responseMessage = mvcResult.getResponse().getContentAsString();
        final var token = objectMapper.readValue(responseMessage, Token.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/secured")
                        .header("Authorization", "Bearer " + token.token()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatus401ForUnauthorizedUser() throws Exception {
        final var userCredentials = new UserCredentialsForm("mockUser1", "mockPassword1");
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(userCredentials))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnStatus401ForExpiredToken() throws Exception {
        final var token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb2NrVXNlciIsImlhdCI6MTY4MzExNjE4OCwiZXhwIjoxNjgzMTE2MTg4fQ._EsYDyw7ia3ywOBJbvuHBL17toVwGIbYJzChuEZ2mZk";
        mockMvc.perform(MockMvcRequestBuilders.get("/secured")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnStatus401ForMalformedToken() throws Exception {
        final var token = "malformedToken";
        mockMvc.perform(MockMvcRequestBuilders.get("/secured")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}