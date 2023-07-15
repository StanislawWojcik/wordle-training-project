package stasiek.wojcik.wordletrainingproject.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import stasiek.wojcik.wordletrainingproject.entity.Role;
import stasiek.wojcik.wordletrainingproject.entity.Token;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.UserCredentialsForm;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {


    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public void register(final UserCredentialsForm request) {
        final var user = new User(request.username(), passwordEncoder.encode(request.password()), Role.USER);
        repository.save(user);
        log.info("New user '" + user.getUsername() + "' added to database.");
    }

    public Token authenticate(final UserCredentialsForm request) throws UsernameNotFoundException {
        final var authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        if (authentication.isAuthenticated()) {
            return new Token(jwtTokenService.generateToken(request.username()));
        } else throw new UsernameNotFoundException("User not found.");
    }
}
