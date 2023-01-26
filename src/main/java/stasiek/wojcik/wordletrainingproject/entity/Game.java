package stasiek.wojcik.wordletrainingproject.entity;

import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document
@RequiredArgsConstructor
public class Game {

    private String word;
    private int attemptsCounter;
    @NonNull
    private Map<Character, LetterResult> keyboard;
    private SessionStatus status;


    public void incrementAttemptsCounter() {
        this.attemptsCounter++;
    }

    public boolean isGameValid() {
        return attemptsCounter < 6 && status.equals(SessionStatus.IN_PROGRESS);
    }

}
