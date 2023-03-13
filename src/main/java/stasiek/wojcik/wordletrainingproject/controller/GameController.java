package stasiek.wojcik.wordletrainingproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.service.GameService;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class GameController {


    private final GameService gameService;

    @PostMapping("/start-game")
    public ResponseEntity<String> startGame(final Principal principal) throws IOException {
        gameService.startNewGame(principal.getName());
        return new ResponseEntity<>("New game started.", HttpStatus.OK);
    }
}

