package stasiek.wojcik.wordletrainingproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordGeneratorTest {

    private WordGenerator wordGenerator;

    @BeforeEach
    void setup() {
        wordGenerator = new WordGenerator();
    }

    @Test
    void shouldReturnSingleWordFromImportedList() {
        var generatedWord = wordGenerator.generateWord();
        assertFalse(generatedWord.isEmpty());
    }

    @Test
    void shouldReturnTrueForWordThatIsOnImportedList() {
        var word = "guess";
        assertTrue(wordGenerator.isOnWordList(word));
    }

    @Test
    void shouldReturnFalseForWordThatIsNotOnImportedList() {
        var word = "inval";
        assertFalse(wordGenerator.isOnWordList(word));
    }
}