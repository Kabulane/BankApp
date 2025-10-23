package fr.exalt.bankaccount.infrastructure.rest.exception;

import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.domain.model.exception.*;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AccountNotFoundApplicationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAccountNotFound(AccountNotFoundApplicationException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusiness(BusinessRuleViolationException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDomain(DomainException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CeilingExceededException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String handleCeiling(CeilingExceededException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String handleInsufficientFunds(InsufficientFundsException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(InvariantViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInvariantViolation(InvariantViolationException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Unexpected error");
        pd.setDetail("An unexpected error occurred.");
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }
}
