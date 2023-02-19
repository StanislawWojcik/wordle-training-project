package stasiek.wojcik.wordletrainingproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.GuessResponse;
import stasiek.wojcik.wordletrainingproject.entity.LetterGuessResult;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

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


    public void startNewGame(String username) {
        var user = repository.findUserByUsername(username).orElseThrow();
        var game = Game.builder()
                .attemptsCounter(0)
                .keyboard(generateKeyboardMap())
                // TODO: need to implement 5-character word generator instead of hardcode
                .word("kayak")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        user.setGame(game);
        repository.save(user);
    }

    public GuessResponse guess(String username, String guess) {
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

    private List<LetterGuessResult> processGuess(Game game, String guess) {
        List<LetterGuessResult> resultList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (game.getWord().charAt(i) == guess.charAt(i)) {
                updateKeyboard(guess.charAt(i), game.getKeyboard(), LetterResult.CORRECT);
                resultList.add(new LetterGuessResult(i, String.valueOf(guess.charAt(i)), LetterResult.CORRECT));
            } else if (game.getWord().contains(String.valueOf(guess.charAt(i)))) {
                updateKeyboard(guess.charAt(i), game.getKeyboard(), LetterResult.INCORRECT_POSITION);
                resultList.add(new LetterGuessResult(i, String.valueOf(guess.charAt(i)), LetterResult.INCORRECT_POSITION));
            } else {
                updateKeyboard(guess.charAt(i), game.getKeyboard(), LetterResult.ABSENT);
                resultList.add(new LetterGuessResult(i, String.valueOf(guess.charAt(i)), LetterResult.ABSENT));
            }
        }
        updateStatusForWin(game, guess);
        game.setLastGuess(guess);
        return resultList;
    }

    private void updateStatusForWin(Game game, String guess) {
        if (game.getWord().equals(guess)) {
            game.setStatus(SessionStatus.WIN);
        } else if (game.getAttemptsCounter() == 6) {
            game.setStatus(SessionStatus.FAILED);
        }
    }

    private void updateKeyboard(Character character, Map<Character, LetterResult> keyboard, LetterResult letterResult) {
        if (!keyboard.get(character).equals(LetterResult.CORRECT)) {
            keyboard.replace(character, letterResult);
        }
    }

    private Map<Character, LetterResult> generateKeyboardMap() {
        return ALPHABET.stream().collect(Collectors.toMap(value -> value, value -> LetterResult.NOT_USED));
    }

}
