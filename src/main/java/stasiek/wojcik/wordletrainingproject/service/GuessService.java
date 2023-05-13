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
                .map(user -> isGuessValid(guess) && user.getGame() != null
                        ? processExistingGame(user, guess)
                        : null);
    }

    private GuessResponse processExistingGame(final User user, final String guess) {
        final var game = user.getGame();
        final var letterGuessResults = game.isGameValid()
                ? processValidGame(user, guess)
                : processGuess(game, game.getLastGuess());
        return new GuessResponse(game.getAttemptsCounter(), game.getStatus(), letterGuessResults, game.getKeyboard());
    }

    private List<LetterGuessResult> processValidGame(final User user, final String guess) {
        final var game = user.getGame();
        game.incrementAttemptsCounter();
        final var letterGuessResults = processGuess(game, guess);
        repository.save(user);
        return letterGuessResults;
    }

    private List<LetterGuessResult> processGuess(final Game game, final String guess) {
        final var word = game.getWord();
        final var progress = new GameProgress(word, guess);
        for (int i = 0; i < 5; i++) {
            progress.setIndex(i);
            if (isLetterCorrect(progress)) {
                validateCorrectLetter(progress);
            } else if (isWordContainingLetter(progress)) {
                validateIncorrectLetter(progress);
            } else {
                progress.getAbsent().add(new LetterGuessResult(i, guess.charAt(i), LetterResult.ABSENT));
            }
        }
        updateStatusForWin(game, guess);
        game.setLastGuess(guess);
        final var completeResultList = buildCompleteList(progress);
        updateKeyboard(completeResultList, game.getKeyboard());
        return completeResultList;
    }

    private void validateCorrectLetter(final GameProgress progress) {
        final var guess = progress.getGuess();
        final var word = progress.getWord();
        final var index = progress.getIndex();
        final var misplacedLetters = getLettersFromList(progress.getMisplaced(), guess.charAt(index));
        final var misplacedSpecificLetter = misplacedLetters.stream().findFirst();
        final var letterGuessResult = new LetterGuessResult(index, word.charAt(index), LetterResult.CORRECT);
        if (misplacedSpecificLetter.isPresent()) {
            if (!hasWordMoreLettersThanProgress(progress)) {
                progress.getAbsent().add(new LetterGuessResult(misplacedSpecificLetter.get().index(),
                        guess.charAt(index), LetterResult.ABSENT));
                progress.getMisplaced().remove(misplacedSpecificLetter.get());
            }
            progress.getCorrect().add(letterGuessResult);
        } else {
            progress.getCorrect().add(letterGuessResult);
        }
    }

    private void validateIncorrectLetter(final GameProgress progress) {
        final var guess = progress.getGuess();
        final var index = progress.getIndex();
        if (hasWordMoreLettersThanProgress(progress)) {
            progress.getMisplaced().add(new LetterGuessResult(index, guess.charAt(index),
                    LetterResult.INCORRECT_POSITION));
        } else {
            progress.getAbsent().add(new LetterGuessResult(index, guess.charAt(index), LetterResult.ABSENT));
        }
    }

    private boolean isWordContainingLetter(final GameProgress progress) {
        return progress.getWord().contains(String.valueOf(progress.getGuess().charAt(progress.getIndex())));
    }

    private boolean isLetterCorrect(final GameProgress progress) {
        return progress.getWord().charAt(progress.getIndex()) == progress.getGuess().charAt(progress.getIndex());
    }

    private boolean hasWordMoreLettersThanProgress(final GameProgress progress) {
        final var guess = progress.getGuess();
        final var word = progress.getWord();
        final var index = progress.getIndex();
        final var misplacedLetters = getLettersFromList(progress.getMisplaced(), guess.charAt(index));
        final var correctLetters = getLettersFromList(progress.getCorrect(), guess.charAt(index));
        return misplacedLetters.size() + correctLetters.size()
                < StringUtils.countOccurrencesOf(word, String.valueOf(guess.charAt(index)));
    }

    private List<LetterGuessResult> getLettersFromList(final List<LetterGuessResult> letters, final char letterAtIndex) {
        return letters.stream().filter(letter -> letter.letter().equals(letterAtIndex)).toList();
    }

    private List<LetterGuessResult> buildCompleteList(final GameProgress progress) {
        return Stream.of(progress.getCorrect(), progress.getMisplaced(), progress.getAbsent())
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