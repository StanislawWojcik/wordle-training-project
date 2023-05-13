package stasiek.wojcik.wordletrainingproject.entity;

import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;

public record LetterGuessResult(int index,
                                Character letter,
                                LetterResult guessResult) {
}
