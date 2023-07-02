package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("ru.yandex.practicum.filmorate")
@Slf4j
public class FilmorateExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        log.warn("Validation error --- " + e.getMessage());
        return new ErrorResponse("Validation error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.warn("Search error --- " + e.getMessage());
        return new ErrorResponse("Search error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        for (ObjectError err : e.getAllErrors()) {
            sb.append(e.getParameter().getParameterName()).append(": ");
            sb.append(err.getDefaultMessage()).append(". ");
        }
        sb.delete(sb.length() - 2, sb.length());
        log.warn("Validation error --- " + sb);
        return new ErrorResponse("Validation error", sb.toString());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSQLException(final SQLWorkException e) {
        log.warn("SQL Error: ", e);
        return new ErrorResponse("SQL Error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(final Exception e) {
        log.warn("Error: ", e);
        return new ErrorResponse("Error: ", e.getMessage());
    }
}
