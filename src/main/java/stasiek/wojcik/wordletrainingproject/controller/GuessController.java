package stasiek.wojcik.wordletrainingproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.entity.GuessRequest;
import stasiek.wojcik.wordletrainingproject.service.GameService;
import stasiek.wojcik.wordletrainingproject.service.GuessService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public final class GuessController {

    private final GuessService guessService;
    private final GameService gameService;

    @PostMapping("/guess")
    public ResponseEntity<Object> guess(@RequestBody final GuessRequest guessRequest,
                                        final Principal principal) {
        if (guessRequest.guess() == null || !validateGuess(guessRequest.guess())) {
            return new ResponseEntity<>("Incorrect input.", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        final var guessResponse =
                guessService.guess(principal.getName(), guessRequest.guess().toLowerCase());
        if (guessResponse.isPresent()) {
            return new ResponseEntity<>(guessResponse, HttpStatus.OK);
        } else return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private boolean validateGuess(final String guess) {
        return guess.length() == 5
                && guess.chars().allMatch(Character::isLetter)
                && gameService.isOnWordList(guess);
    }
}
