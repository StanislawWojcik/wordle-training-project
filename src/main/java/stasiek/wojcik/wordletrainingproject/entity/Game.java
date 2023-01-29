package stasiek.wojcik.wordletrainingproject.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;

import java.util.Map;

@Data
@Builder
@Document
@AllArgsConstructor
public class Game {

    private String word;
    private int attemptsCounter;
    private Map<Character, LetterResult> keyboard;
    private SessionStatus status;


    public void incrementAttemptsCounter() {
        this.attemptsCounter++;
    }

    public boolean isGameValid() {
        return attemptsCounter < 6 && status.equals(SessionStatus.IN_PROGRESS);
    }

}
