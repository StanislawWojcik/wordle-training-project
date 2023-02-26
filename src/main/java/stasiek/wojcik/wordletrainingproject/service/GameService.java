package stasiek.wojcik.wordletrainingproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.GuessResponse;
import stasiek.wojcik.wordletrainingproject.entity.LetterGuessResult;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository repository;


    // TODO: alphabet to be moved somewhere else
    private final static List<Character> ALPHABET = Arrays.asList('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z');


    // TODO: move startNewGame and generateWord to separated service classes

    public void startNewGame(final String username) throws IOException {
        final var user = repository.findUserByUsername(username).orElseThrow();
        final String word = generateWord();
        final var game = Game.builder()
                .attemptsCounter(0)
                .keyboard(generateKeyboardMap())
                // TODO: need to implement 5-character word generator instead of hardcode
                .word(word)
                .status(SessionStatus.IN_PROGRESS)
                .build();
        user.setGame(game);
        System.out.println("GENERATED WORD IS : " + word);
        repository.save(user);
    }

    private String generateWord() throws IOException {
        final File file = ResourceUtils.getFile("classpath:word-list.txt");
        final String randomWord;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            final long randomLocation = (long) (Math.random() * file.length());
            randomAccessFile.seek(randomLocation);
            randomAccessFile.readLine();
            randomWord = randomAccessFile.readLine();
        }
        return randomWord;
    }

    public GuessResponse guess(final String username,
                               final String guess) {
        var user = repository.findUserByUsername(username).orElseThrow();
        var game = user.getGame();
        List<LetterGuessResult> letterGuessResults;
        if (game != null && game.isGameValid()) {
            game.incrementAttemptsCounter();
            letterGuessResults = processGuess(game, guess);
            repository.save(user);
        } else if (game != null && !game.isGameValid()) {
            letterGuessResults = processGuess(game, game.getLastGuess());
        } else return null;
        return new GuessResponse(game.getAttemptsCounter(), game.getStatus(), letterGuessResults, game.getKeyboard());
    }

    private List<LetterGuessResult> processGuess(final Game game,
                                                 final String guess) {
        List<LetterGuessResult> resultList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (game.getWord().charAt(i) == guess.charAt(i)) {
                updateKeyboard(guess.charAt(i), game.getKeyboard(), LetterResult.CORRECT);
                resultList.add(new LetterGuessResult(i, String.valueOf(guess.charAt(i)), LetterResult.CORRECT));
            } else if (game.getWord().contains(String.valueOf(guess.charAt(i)))) {
                updateKeyboard(guess.charAt(i), game.getKeyboard(), LetterResult.INCORRECT_POSITION);
                resultList.add(new LetterGuessResult(i, String.valueOf(guess.charAt(i)), setResult(game.getWord(), guess, guess.charAt(i), i)));
            } else {
                updateKeyboard(guess.charAt(i), game.getKeyboard(), LetterResult.ABSENT);
                resultList.add(new LetterGuessResult(i, String.valueOf(guess.charAt(i)), LetterResult.ABSENT));
            }
        }
        updateStatusForWin(game, guess);
        game.setLastGuess(guess);
        return resultList;
    }

    private LetterResult setResult(final String word,
                                   final String guess,
                                   final char letter,
                                   final int index) {
        final int inWord = StringUtils.countOccurrencesOf(word, String.valueOf(letter));
        final int inGuess = StringUtils.countOccurrencesOf(guess, String.valueOf(letter));
        if (inWord == inGuess) {
            return LetterResult.INCORRECT_POSITION;
        } else if (inWord > inGuess) {
            return LetterResult.INCORRECT_POSITION;
        } else {
            return guess.indexOf(letter) == index ? LetterResult.INCORRECT_POSITION : LetterResult.ABSENT;
        }
    }

    private void updateStatusForWin(final Game game,
                                    final String guess) {
        if (game.getWord().equals(guess)) {
            game.setStatus(SessionStatus.WIN);
        } else if (game.getAttemptsCounter() == 6) {
            game.setStatus(SessionStatus.FAILED);
        }
    }

    private void updateKeyboard(final Character character,
                                final Map<Character, LetterResult> keyboard,
                                final LetterResult letterResult) {
        if (!keyboard.get(character).equals(LetterResult.CORRECT)) {
            keyboard.replace(character, letterResult);
        }
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return ALPHABET.stream().collect(Collectors.toMap(value -> value, value -> LetterResult.NOT_USED));
    }

}
