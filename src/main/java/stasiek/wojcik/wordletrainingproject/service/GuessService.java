package stasiek.wojcik.wordletrainingproject.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import stasiek.wojcik.wordletrainingproject.entity.*;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.util.*;
import java.util.stream.Stream;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GuessService {

    private final UserRepository repository;
    private final WordGenerator wordGenerator;

    private String guess;
    private String word;
    private User user;
    private Game game;
    private GameProgress progress;
    private Map<Character, LetterResult> keyboard;
    private List<LetterGuessResult> resultList;


    public GuessService(UserRepository repository, WordGenerator wordGenerator) {
        this.repository = repository;
        this.wordGenerator = wordGenerator;
    }

    public Optional<GuessResponse> processGuess(final String username, final String guess) {
        return repository.findUserByUsername(username)
                .map(user -> {
                    this.guess = guess;
                    this.user = user;
                    return processGuessForValidUser();
                });
    }

    private GuessResponse processGuessForValidUser() {
        return Optional.ofNullable(user.getGame())
                .map(game -> {
                    this.game = game;
                    this.word = game.getWord();
                    if (isGuessValid()) {
                        return processExistingGame();
                    } else return null;
                })
                .orElse(null);
    }

    private GuessResponse processExistingGame() {
        List<LetterGuessResult> letterGuessResults;
        if (!game.isGameValid()) {
            this.guess = game.getLastGuess();
            letterGuessResults = processGuess();
        } else {
            letterGuessResults = processValidGame();
        }

        return new GuessResponse(game.getAttemptsCounter(), game.getStatus(), letterGuessResults, game.getKeyboard());
    }

    private List<LetterGuessResult> processValidGame() {
        game.incrementAttemptsCounter();
        final var letterGuessResults = processGuess();
        repository.save(user);
        return letterGuessResults;
    }

    private List<LetterGuessResult> processGuess() {
        this.progress = new GameProgress();
        iterateOverLetters();
        updateStatusForWin();
        game.setLastGuess(guess);
        this.resultList = buildCompleteList();
        this.keyboard = game.getKeyboard();
        updateKeyboard();
        return resultList;
    }

    private void iterateOverLetters() {
        for (int i = 0; i < 5; i++) {
            if (word.charAt(i) == guess.charAt(i)) {
                validateCorrectLetter(i);
            } else if (word.contains(String.valueOf(guess.charAt(i)))) {
                validateIncorrectLetter(i);
            } else {
                progress.getAbsent().add(new LetterGuessResult(i, guess.charAt(i), LetterResult.ABSENT));
            }
        }
    }

    private void validateCorrectLetter(int index) {
        final var misplacedLetters = getLettersFromList(progress.getMisplaced(), guess.charAt(index));
        final var correctLetters = getLettersFromList(progress.getCorrect(), guess.charAt(index));
        final var misplacedLetter = misplacedLetters.stream().findFirst();
        final var letterGuessResult = new LetterGuessResult(index, word.charAt(index), LetterResult.CORRECT);
        final var sumOfMisplacedAndCorrect = misplacedLetters.size() + correctLetters.size();
        misplacedLetter.ifPresent(letter ->
                moveLettersBetweenProgressLists(index, misplacedLetter.get(), sumOfMisplacedAndCorrect));
        progress.getCorrect().add(letterGuessResult);
    }

    private void moveLettersBetweenProgressLists(int index, LetterGuessResult misplacedLetter, int sumOfMisplacedAndCorrect) {
        if (!hasWordMoreLetters(sumOfMisplacedAndCorrect, word.charAt(index))) {
            progress.getAbsent().add(new LetterGuessResult(misplacedLetter.index(),
                    guess.charAt(index), LetterResult.ABSENT));
            progress.getMisplaced().remove(misplacedLetter);
        }
    }

    private void validateIncorrectLetter(int index) {
        final var misplacedLetters = getLettersFromList(progress.getMisplaced(), guess.charAt(index));
        final var correctLetters = getLettersFromList(progress.getCorrect(), guess.charAt(index));
        final var sumOfMisplacedAndCorrect = misplacedLetters.size() + correctLetters.size();
        if (hasWordMoreLetters(sumOfMisplacedAndCorrect, guess.charAt(index))) {
            progress.getMisplaced().add(new LetterGuessResult(index, guess.charAt(index),
                    LetterResult.INCORRECT_POSITION));
        } else {
            progress.getAbsent().add(new LetterGuessResult(index, guess.charAt(index), LetterResult.ABSENT));
        }
    }

    private boolean hasWordMoreLetters(final int sumOfMisplacedAndCorrect, final char letter) {
        return sumOfMisplacedAndCorrect < StringUtils.countOccurrencesOf(word, String.valueOf(letter));
    }

    private List<LetterGuessResult> getLettersFromList(final List<LetterGuessResult> letters, final char letterAtIndex) {
        return letters.stream().filter(letter -> letter.letter().equals(letterAtIndex)).toList();
    }

    private List<LetterGuessResult> buildCompleteList() {
        return Stream.of(progress.getCorrect(), progress.getMisplaced(), progress.getAbsent())
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(LetterGuessResult::index))
                .toList();
    }

    private void updateStatusForWin() {
        if (game.getWord().equals(guess)) {
            game.setStatus(SessionStatus.WIN);
        } else if (game.getAttemptsCounter() == 6) {
            game.setStatus(SessionStatus.FAILED);
        }
    }

    private void updateKeyboard() {
        resultList.forEach(letter -> {
            if (!keyboard.get(letter.letter()).equals(LetterResult.CORRECT)) {
                keyboard.replace(letter.letter(), letter.guessResult());
            }
        });
    }

    private boolean isGuessValid() {
        return guess.length() == 5
                && guess.chars().allMatch(Character::isLetter)
                && wordGenerator.isOnWordList(guess);
    }
}