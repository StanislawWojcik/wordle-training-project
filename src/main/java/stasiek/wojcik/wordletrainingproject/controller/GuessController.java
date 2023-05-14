package stasiek.wojcik.wordletrainingproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.entity.GuessRequest;
import stasiek.wojcik.wordletrainingproject.entity.GuessResponse;
import stasiek.wojcik.wordletrainingproject.exception.InvalidGuessException;
import stasiek.wojcik.wordletrainingproject.exception.NoGameFoundException;
import stasiek.wojcik.wordletrainingproject.service.GuessService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public final class GuessController {

    private final GuessService guessService;

    @PostMapping("/guess")
    public ResponseEntity<GuessResponse> guess(@RequestBody final GuessRequest guessRequest, final Principal principal)
            throws NoGameFoundException, InvalidGuessException {
        final var guessResponse = guessService.processGuess(principal.getName(), guessRequest.guess().toLowerCase());
        return new ResponseEntity<>(guessResponse, HttpStatus.OK);
    }
}
