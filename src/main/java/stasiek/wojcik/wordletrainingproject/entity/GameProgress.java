package stasiek.wojcik.wordletrainingproject.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameProgress {

    private final List<LetterGuessResult> correct;
    private final List<LetterGuessResult> misplaced;
    private final List<LetterGuessResult> absent;

    public GameProgress() {
        this.correct = new ArrayList<>();
        this.misplaced = new ArrayList<>();
        this.absent = new ArrayList<>();
    }
}
