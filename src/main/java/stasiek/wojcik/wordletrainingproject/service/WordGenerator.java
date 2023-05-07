package stasiek.wojcik.wordletrainingproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WordGenerator {

    private final static String RESOURCE_LOCATION = "classpath:words";
    private final List<String> wordList = getWordList();

    public String generateWord() {
        var word = wordList.get(new Random().nextInt(wordList.size()));
        System.out.println("WORD : " + word);
        return word;
    }

    public boolean isOnWordList(final String word) {
        return wordList.contains(word);
    }

    private List<String> getWordList() {
        try {
            final var file = ResourceUtils.getFile(RESOURCE_LOCATION);
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload word list. Application will not start.");
        }
    }
}
