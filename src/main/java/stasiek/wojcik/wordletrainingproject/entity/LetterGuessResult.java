package stasiek.wojcik.wordletrainingproject.entity;

import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LetterGuessResult {

    private int index;
    private String letter;
    private LetterResult guessResult;

}
