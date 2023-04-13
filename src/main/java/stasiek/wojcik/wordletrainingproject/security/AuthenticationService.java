package stasiek.wojcik.wordletrainingproject.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
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
public class AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public void register(final UserCredentialsForm request) throws DuplicateKeyException {
        final var user = new User(request.username(), passwordEncoder.encode(request.password()), Role.USER);
        repository.save(user);
        logger.info("New user '" + user.getUsername() + "' added to database.");
    }

    public Token authenticate(final UserCredentialsForm request) throws UsernameNotFoundException {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        final var user = repository.findUserByUsername(request.username());
        return user
                .map(existingUser -> new Token(jwtTokenService.generateToken(existingUser)))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
