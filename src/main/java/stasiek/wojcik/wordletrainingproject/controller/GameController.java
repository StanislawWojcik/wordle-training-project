package stasiek.wojcik.wordletrainingproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.service.GameService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "wordle-training-project")
public final class GameController {

    private final GameService gameService;

    @PostMapping("/start-game")
    @Operation(summary = "Starts a new game.")
    public ResponseEntity<String> startGame(final Principal principal) {
        return gameService.startNewGame(principal.getName())
                .map(game -> new ResponseEntity<>("New game started.", HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }
}

