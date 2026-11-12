package no.lavankumar.sentinelpay.api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleStatus(ResponseStatusException ex) {
        ProblemDetail p=ProblemDetail.forStatusAndDetail(ex.getStatusCode(),ex.getReason());
        p.setTitle("Request could not be completed");p.setType(URI.create("https://sentinelpay.local/problems/request"));return p;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail p=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Request validation failed");
        p.setTitle("Invalid request");p.setType(URI.create("https://sentinelpay.local/problems/validation"));return p;
    }
}
