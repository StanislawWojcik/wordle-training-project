package stasiek.wojcik.wordletrainingproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import stasiek.wojcik.wordletrainingproject.entity.*;
import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GuessService {

    private final UserRepository repository;
    private final WordGenerator wordGenerator;

    public Optional<GuessResponse> processGuess(final String username, final String guess) {
        return repository.findUserByUsername(username)
                .map(user -> processGuessForValidUser(user, guess));
    }

    private GuessResponse processGuessForValidUser(final User user, final String guess) {
        return Optional.of(user.getGame())
                .map(game -> {
                    if (isGuessValid(guess)) {
                        return processExistingGame(game, guess, user);
                    } else return null;
                })
                .orElse(null);
    }

    private GuessResponse processExistingGame(final Game game, final String guess, final User user) {
        final var letterGuessResults = game.isGameValid()
                ? processValidGame(game, guess, user)
                : processGuess(game, game.getLastGuess());
        return new GuessResponse(game.getAttemptsCounter(), game.getStatus(), letterGuessResults, game.getKeyboard());
    }

    private List<LetterGuessResult> processValidGame(final Game game, final String guess, final User user) {
        game.incrementAttemptsCounter();
        final var letterGuessResults = processGuess(game, guess);
        repository.save(user);
        return letterGuessResults;
    }

    private List<LetterGuessResult> processGuess(final Game game, final String guess) {
        final var word = game.getWord();
        final var progress = new GameProgress();
        for (int i = 0; i < 5; i++) {
            if (word.charAt(i) == guess.charAt(i)) {
                validateCorrectLetter(progress, word, guess, i);
            } else if (word.contains(String.valueOf(guess.charAt(i)))) {
                validateIncorrectLetter(progress, word, guess, i);
            } else {
                progress.getAbsent().add(new LetterGuessResult(i, guess.charAt(i), LetterResult.ABSENT));
            }
        }
        updateStatusForWin(game, guess);
        game.setLastGuess(guess);
        final var completeResultList =
                buildCompleteList(progress.getCorrect(), progress.getMisplaced(), progress.getAbsent());
        updateKeyboard(completeResultList, game.getKeyboard());
        return completeResultList;
    }

    private void validateCorrectLetter(final GameProgress progress, final String word, final String guess, final int index) {
        final var misplacedLetters = getLettersFromList(progress.getMisplaced(), guess.charAt(index));
        final var correctLetters = getLettersFromList(progress.getCorrect(), guess.charAt(index));
        final var misplacedLetter = misplacedLetters.stream().findFirst();
        final var letterGuessResult = new LetterGuessResult(index, word.charAt(index), LetterResult.CORRECT);
        if (misplacedLetter.isPresent()) {
            if (!hasWordMoreLetters(misplacedLetters, correctLetters, word, word.charAt(index))) {
                progress.getAbsent().add(new LetterGuessResult(misplacedLetter.get().index(),
                        guess.charAt(index), LetterResult.ABSENT));
                progress.getMisplaced().remove(misplacedLetter.get());
            }
            progress.getCorrect().add(letterGuessResult);
        } else {
            progress.getCorrect().add(letterGuessResult);
        }
    }

    private void validateIncorrectLetter(final GameProgress progress, final String word, final String guess, final int index) {
        final var misplacedLetters = getLettersFromList(progress.getMisplaced(), guess.charAt(index));
        final var correctLetters = getLettersFromList(progress.getCorrect(), guess.charAt(index));
        if (hasWordMoreLetters(misplacedLetters, correctLetters, word, guess.charAt(index))) {
            progress.getMisplaced().add(new LetterGuessResult(index, guess.charAt(index),
                    LetterResult.INCORRECT_POSITION));
        } else {
            progress.getAbsent().add(new LetterGuessResult(index, guess.charAt(index), LetterResult.ABSENT));
        }
    }

    private boolean hasWordMoreLetters(final List<LetterGuessResult> misplaced, final List<LetterGuessResult> correct,
                                       final String word, final char letter) {
        return misplaced.size() + correct.size() < StringUtils.countOccurrencesOf(word, String.valueOf(letter));
    }

    private List<LetterGuessResult> getLettersFromList(final List<LetterGuessResult> letters, final char letterAtIndex) {
        return letters.stream().filter(letter -> letter.letter().equals(letterAtIndex)).toList();
    }

    private List<LetterGuessResult> buildCompleteList(final List<LetterGuessResult> correct,
                                                      final List<LetterGuessResult> incorrectPosition,
                                                      final List<LetterGuessResult> absent) {
        return Stream.of(correct, incorrectPosition, absent)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(LetterGuessResult::index))
                .toList();
    }

    private void updateStatusForWin(final Game game, final String guess) {
        if (game.getWord().equals(guess)) {
            game.setStatus(SessionStatus.WIN);
        } else if (game.getAttemptsCounter() == 6) {
            game.setStatus(SessionStatus.FAILED);
        }
    }

    private void updateKeyboard(final List<LetterGuessResult> resultList, final Map<Character, LetterResult> keyboard) {
        resultList.forEach(letter -> {
            if (!keyboard.get(letter.letter()).equals(LetterResult.CORRECT)) {
                keyboard.replace(letter.letter(), letter.guessResult());
            }
        });
    }

    private boolean isGuessValid(final String guess) {
        return guess.length() == 5
                && guess.chars().allMatch(Character::isLetter)
                && wordGenerator.isOnWordList(guess);
    }
}