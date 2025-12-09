package be.tackit.lottosysteem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class KlantNietGevondenException extends RuntimeException {
    public KlantNietGevondenException(long id) {
        super("klant met id: " + id + "niet gevonden.");
    }
}
