package stasiek.wojcik.wordletrainingproject.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            GlobalExceptionHandler.class);


    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> handleDuplicateKeyException(final Exception e) {
        logger.error("DuplicateKeyException - user already in database.");
        return new ResponseEntity<>("User already exists in database.", HttpStatus.CONFLICT);
    }
}
