package com.adventurebook.api;

import com.adventurebook.model.InvalidChoiceException;
import com.adventurebook.service.NotFoundException;
import com.adventurebook.service.UnplayableBookException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UnplayableBookException.class)
    public ProblemDetail unplayable(UnplayableBookException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problem.setProperty("errors", e.errors());
        return problem;
    }

    @ExceptionHandler(InvalidChoiceException.class)
    public ProblemDetail invalidChoice(InvalidChoiceException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
