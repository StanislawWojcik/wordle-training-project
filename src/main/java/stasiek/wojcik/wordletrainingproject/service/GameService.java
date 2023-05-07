package stasiek.wojcik.wordletrainingproject.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository repository;
    private final WordGenerator wordGenerator;

    @Value("${alphabet}")
    private final List<Character> ALPHABET;

    public Optional<Game> startNewGame(final String username) {
        return repository.findUserByUsername(username)
                .map(this::startGameForExistingUser);
    }

    private Game startGameForExistingUser(final User user) {
        final var word = wordGenerator.generateWord();
        final var game = Game.builder()
                .attemptsCounter(0)
                .keyboard(generateKeyboardMap())
                .word(word)
                .status(SessionStatus.IN_PROGRESS)
                .build();
        user.setGame(game);
        System.out.println("GENERATED WORD IS : " + word);
        repository.save(user);
        return game;
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return ALPHABET.stream().collect(Collectors.toMap(value -> value, value -> LetterResult.NOT_USED));
    }
}
