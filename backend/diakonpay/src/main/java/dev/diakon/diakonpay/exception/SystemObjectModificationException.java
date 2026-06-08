package dev.diakon.diakonpay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SystemObjectModificationException extends RuntimeException {
    public SystemObjectModificationException(String message) {
        super(message);
    }
}
