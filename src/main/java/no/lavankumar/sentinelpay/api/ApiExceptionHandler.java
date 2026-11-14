package no.lavankumar.sentinelpay.api;

import java.net.URI;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleStatus(ResponseStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        problem.setTitle("Request could not be completed");
        problem.setType(URI.create("https://sentinelpay.local/problems/request"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Request validation failed");
        problem.setTitle("Invalid request");
        problem.setType(URI.create("https://sentinelpay.local/problems/validation"));
        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleConcurrentReview(OptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "This case was updated by another analyst. Refresh the case before trying again.");
        problem.setTitle("Concurrent case update");
        problem.setType(URI.create("https://sentinelpay.local/problems/concurrent-update"));
        return problem;
    }
}
