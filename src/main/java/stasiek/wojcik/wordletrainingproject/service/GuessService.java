package stasiek.wojcik.wordletrainingproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import stasiek.wojcik.wordletrainingproject.entity.Game;
import stasiek.wojcik.wordletrainingproject.entity.GuessResponse;
import stasiek.wojcik.wordletrainingproject.entity.LetterGuessResult;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuessService {

    private final UserRepository repository;


    // TODO: change guess response from record to regular object
    public GuessResponse guess(final String username,
                               final String guess) {
        var user = repository.findUserByUsername(username).orElseThrow();
        var game = user.getGame();
        List<LetterGuessResult> letterGuessResults = null;
        if (game != null && game.isGameValid()) {
            game.incrementAttemptsCounter();
            letterGuessResults = processGuess(game, guess);
            repository.save(user);
        } else if (game != null && !game.isGameValid()) {
            letterGuessResults = processGuess(game, game.getLastGuess());
        }
        return letterGuessResults != null ? new GuessResponse(game, letterGuessResults) : null;
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



}
