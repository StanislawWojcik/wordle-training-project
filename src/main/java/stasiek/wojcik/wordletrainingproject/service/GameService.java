package stasiek.wojcik.wordletrainingproject.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    @Value("${alphabet}")
    private final List<Character> ALPHABET;

    private final UserRepository repository;

    public void startNewGame(final String username) throws IOException {
        final var user = repository.findUserByUsername(username).orElseThrow();
        final String word = generateWord();
        final var game = Game.builder()
                .attemptsCounter(0)
                .keyboard(generateKeyboardMap())
                .word(word)
                .status(SessionStatus.IN_PROGRESS)
                .build();
        user.setGame(game);
        System.out.println("GENERATED WORD IS : " + word);
        repository.save(user);
    }

    private String generateWord() throws IOException {
        final File file = ResourceUtils.getFile("classpath:wordle-nyt-allowed-guesses.txt");
        final String randomWord;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            final long randomLocation = (long) (Math.random() * file.length());
            randomAccessFile.seek(randomLocation);
            randomAccessFile.readLine();
            randomWord = randomAccessFile.readLine();
        }
        return randomWord;
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return ALPHABET.stream().collect(Collectors.toMap(value -> value, value -> LetterResult.NOT_USED));
    }
}
