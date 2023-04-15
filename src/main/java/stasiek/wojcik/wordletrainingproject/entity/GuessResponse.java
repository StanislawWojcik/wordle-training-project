package stasiek.wojcik.wordletrainingproject.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;

import java.util.List;
import java.util.Map;

public record GuessResponse(int attempts, SessionStatus status, List<LetterGuessResult> guessLetters,
                            Map<Character, LetterResult> keyboard) {

    public GuessResponse(@JsonProperty("attempts") final int attempts,
                         @JsonProperty("status") final SessionStatus status,
                         @JsonProperty("guessLetters") final List<LetterGuessResult> guessLetters,
                         @JsonProperty("keyboard") final Map<Character, LetterResult> keyboard) {
        this.attempts = attempts;
        this.status = status;
        this.guessLetters = guessLetters;
        this.keyboard = keyboard;
    }
}



