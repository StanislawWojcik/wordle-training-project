package stasiek.wojcik.wordletrainingproject.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.GameProgress;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    @Value("${alphabet}")
    private final List<Character> ALPHABET;

    private final List<String> wordList = getWordList();

    private final UserRepository repository;

    public Optional<Game> startNewGame(final String username) {
        return repository.findUserByUsername(username)
                .map(this::startGameForExistingUser);
    }

    private Game startGameForExistingUser(final User user) {
        final var word = generateWord();
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

    public boolean isOnWordList(final String word) {
        return wordList.contains(word);
    }

    private List<String> getWordList() {
        try {
            final var file = ResourceUtils.getFile("classpath:words");
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload word list. Application will not start.");
        }
    }

    private String generateWord() {
        return wordList.get(new Random().nextInt(wordList.size()));
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return ALPHABET.stream().collect(Collectors.toMap(value -> value, value -> LetterResult.NOT_USED));
    }
}
