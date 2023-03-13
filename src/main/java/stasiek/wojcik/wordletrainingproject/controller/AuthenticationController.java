package stasiek.wojcik.wordletrainingproject.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.entity.Token;
import stasiek.wojcik.wordletrainingproject.entity.UserCredentialsForm;
import stasiek.wojcik.wordletrainingproject.service.AuthenticationService;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerNewUser(@RequestBody final UserCredentialsForm userCredentialsForm) {
        if (authenticationService.register(userCredentialsForm)) {
            return new ResponseEntity<>("User registered.", HttpStatus.OK);
        } else return new ResponseEntity<>("Username already exists.", HttpStatus.CONFLICT);
    }

    @PostMapping("/login")
    public ResponseEntity<Token> authenticateUser(@RequestBody final UserCredentialsForm userCredentialsForm) {
        var token = authenticationService.authenticate(userCredentialsForm);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }
}
