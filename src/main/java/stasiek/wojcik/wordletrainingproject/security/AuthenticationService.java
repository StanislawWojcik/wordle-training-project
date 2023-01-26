package stasiek.wojcik.wordletrainingproject.security;

import stasiek.wojcik.wordletrainingproject.entity.Role;
import stasiek.wojcik.wordletrainingproject.entity.User;
import stasiek.wojcik.wordletrainingproject.entity.RegistrationForm;
import stasiek.wojcik.wordletrainingproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public boolean register(RegistrationForm request) {
        var user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), Role.USER);
        try {
            repository.save(user);
            logger.info("New user '" + user.getUsername() + "' added to database.");
            return true;
        } catch (DuplicateKeyException e) {
            logger.info("User with '" + user.getUsername() + "' username already exists in database.");
            return false;
        }
    }

    public String authenticate(RegistrationForm request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = repository.findUserByUsername(request.getUsername());
        return user.map(jwtTokenService::generateToken).orElse(null);
    }
}
