package stasiek.wojcik.wordletrainingproject.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(final Exception e) {
        return new ResponseEntity<>("User already exists in database.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoGameFoundException.class)
    public ResponseEntity<String> handleNoGameFoundException(final Exception e) {
        return new ResponseEntity<>("User haven't started any games yet.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidGuessException.class)
    public ResponseEntity<String> handleInvalidGuessException(final Exception e) {
        return new ResponseEntity<>("Invalid input provided.", HttpStatus.NOT_ACCEPTABLE);
    }
}
