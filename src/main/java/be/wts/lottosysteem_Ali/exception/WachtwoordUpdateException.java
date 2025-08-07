package be.wts.lottosysteem_Ali.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WachtwoordUpdateException extends RuntimeException {
    public WachtwoordUpdateException(long id) {
        super("WachtwoordUpdate mislukt voor gebruiker met id:" + id);
    }
}
