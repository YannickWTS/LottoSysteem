package be.tackit.lottosysteem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GebruikerNietGevondenException extends RuntimeException {
    public GebruikerNietGevondenException() {
        super("Gebruiker niet gevonden.");
    }
}
