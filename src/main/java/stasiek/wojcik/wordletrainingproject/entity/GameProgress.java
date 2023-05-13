package stasiek.wojcik.wordletrainingproject.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameProgress {

    private final String word;
    private final String guess;
    private final List<LetterGuessResult> correct;
    private final List<LetterGuessResult> misplaced;
    private final List<LetterGuessResult> absent;
    private int index;

    public GameProgress(String word, String guess) {
        this.word = word;
        this.guess = guess;
        this.correct = new ArrayList<>();
        this.misplaced = new ArrayList<>();
        this.absent = new ArrayList<>();
    }
}
