package stasiek.wojcik.wordletrainingproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.entity.GuessRequest;
import stasiek.wojcik.wordletrainingproject.entity.Token;
import stasiek.wojcik.wordletrainingproject.entity.UserCredentialsForm;
import stasiek.wojcik.wordletrainingproject.security.AuthenticationService;
import stasiek.wojcik.wordletrainingproject.service.GameService;

import java.security.Principal;

@RestController
@Validated
public class GameController {

    // TODO: separate auth endpoints to different controller

    @Autowired
    GameService gameService;

    @Autowired
    AuthenticationService authenticationService;


    @PostMapping("/register")
    public ResponseEntity<String> registerNewUser(@RequestBody UserCredentialsForm userCredentialsForm) {
        if (authenticationService.register(userCredentialsForm)) {
            return new ResponseEntity<>("User registered.", HttpStatus.OK);
        } else return new ResponseEntity<>("Username already exists.", HttpStatus.CONFLICT);
    }

    @PostMapping("/login")
    public ResponseEntity<Token> authenticateUser(@RequestBody UserCredentialsForm userCredentialsForm) {
        var token = authenticationService.authenticate(userCredentialsForm);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }


    // TODO: provide proper endpoint for starting a new game
    @PostMapping("/startGame")
    public ResponseEntity<String> startGame(Principal principal) {
        gameService.startNewGame(principal.getName());
        return new ResponseEntity<>("New game started.", HttpStatus.OK);
    }

    @PostMapping("/guess")
    public ResponseEntity<Object> guess(@RequestBody GuessRequest guessRequest, Principal principal) {
        if (!isGuessFormatCorrect(guessRequest.guess())) {
            return new ResponseEntity<>("Incorrect input format", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var guessResponse = gameService.guess(principal.getName(), guessRequest.guess());
        if (guessResponse != null) {
            return new ResponseEntity<>(guessResponse, HttpStatus.OK);
        } else return new ResponseEntity<>("No valid game recognized.", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // TODO: pay attention to case sensitive checks
    private boolean isGuessFormatCorrect(String guess) {
        return guess.length()==5 && guess.chars().allMatch(Character::isLetter);
    }
}

