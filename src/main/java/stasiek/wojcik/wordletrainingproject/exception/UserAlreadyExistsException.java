package stasiek.wojcik.wordletrainingproject.exception;

public class UserAlreadyExistsException extends Exception {

    public UserAlreadyExistsException(final String message) {
        super(message);
    }
}
