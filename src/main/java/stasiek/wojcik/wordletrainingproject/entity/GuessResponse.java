package stasiek.wojcik.wordletrainingproject.entity;

import lombok.Data;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;

import java.util.List;
import java.util.Map;

@Data
public class GuessResponse {

    private final int attempts;
    private final SessionStatus status;
    private final List<LetterGuessResult> guessLetters;
    private final Map<Character, LetterResult> keyboard;

    public GuessResponse(final Game game, final List<LetterGuessResult> guessLetters) {
        this.attempts = game.getAttemptsCounter();
        this.status = game.getStatus();
        this.guessLetters = guessLetters;
        this.keyboard = game.getKeyboard();
    }
}



