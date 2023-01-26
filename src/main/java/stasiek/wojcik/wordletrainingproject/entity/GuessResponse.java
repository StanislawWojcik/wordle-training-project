package stasiek.wojcik.wordletrainingproject.entity;

import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class GuessResponse {

    private int attempts;
    private SessionStatus status;
    private List<LetterGuessResult> guessLetters;

    private Map<Character, LetterResult> keyboard;
}
