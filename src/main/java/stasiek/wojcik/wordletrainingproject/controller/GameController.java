package stasiek.wojcik.wordletrainingproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.service.GameService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public final class GameController {

    private final GameService gameService;

    @PostMapping("/start-game")
    public ResponseEntity<String> startGame(final Principal principal) {
        return gameService.startNewGame(principal.getName())
                .map(game -> new ResponseEntity<>("New game started.", HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }
}

