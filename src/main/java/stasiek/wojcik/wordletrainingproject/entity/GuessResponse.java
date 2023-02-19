package stasiek.wojcik.wordletrainingproject.entity;

import stasiek.wojcik.wordletrainingproject.entity.result.LetterResult;
import stasiek.wojcik.wordletrainingproject.entity.result.SessionStatus;

import java.util.List;
import java.util.Map;

public record GuessResponse(int attempts,
                            SessionStatus status,
                            List<LetterGuessResult> guessLetters,
                            Map<Character, LetterResult> keyboard) {}
