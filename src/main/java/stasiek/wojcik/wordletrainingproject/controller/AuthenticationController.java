package stasiek.wojcik.wordletrainingproject.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stasiek.wojcik.wordletrainingproject.entity.Token;
import stasiek.wojcik.wordletrainingproject.entity.UserCredentialsForm;
import stasiek.wojcik.wordletrainingproject.security.AuthenticationService;

@RestController
@RequiredArgsConstructor
public final class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerNewUser(@RequestBody final UserCredentialsForm userCredentialsForm) {
        authenticationService.register(userCredentialsForm);
        return new ResponseEntity<>("User registered.", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Token> authenticateUser(@RequestBody final UserCredentialsForm userCredentialsForm) {
        try {
            return new ResponseEntity<>(authenticationService.authenticate(userCredentialsForm), HttpStatus.OK);
        } catch (final UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/secured")
    public ResponseEntity<String> secured() {
        return new ResponseEntity<>("Secured.", HttpStatus.OK);
    }
}
