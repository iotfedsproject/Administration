package eu.h2020.symbiote.administration.exceptions;

import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
@RestControllerAdvice
public class CustomExceptionHandler {


    @ExceptionHandler(ServiceValidationException.class)
    public void handleServiceValidationException(ServiceValidationException ex, HttpServletResponse response) throws IOException {

        HttpStatus httpStatus = BAD_REQUEST;

        response.sendError(httpStatus.value(), ex.getMessage());
    }
}