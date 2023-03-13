package stasiek.wojcik.wordletrainingproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.entity.GuessRequest;
import stasiek.wojcik.wordletrainingproject.entity.GuessResponse;
import stasiek.wojcik.wordletrainingproject.service.GuessService;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class GuessController {

    private final GuessService guessService;


    @PostMapping("/guess")
    public ResponseEntity<Object> guess(@RequestBody final GuessRequest guessRequest,
                                        final Principal principal) {
        if (!isGuessFormatCorrect(guessRequest.guess())) {
            return new ResponseEntity<>("Incorrect input format", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<GuessResponse> guessResponse =
                Optional.ofNullable(guessService.guess(principal.getName(), guessRequest.guess().toLowerCase()));
        if (guessResponse.isPresent()) {
            return new ResponseEntity<>(guessResponse, HttpStatus.OK);
        } else return new ResponseEntity<>("No valid game recognized.", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // TODO: pay attention to case sensitive checks
    private boolean isGuessFormatCorrect(final String guess) {
        return guess.length()==5 && guess.chars().allMatch(Character::isLetter);
    }
}
