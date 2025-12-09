package be.tackit.lottosysteem;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String HandleIllegalArgument(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    Map<String,String> onIllegalState(IllegalStateException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String,String> onFK(DataIntegrityViolationException e) {
        return Map.of("error", "Record kan niet verwijderd worden (gerelateerde data bestaat).");
    }
}
