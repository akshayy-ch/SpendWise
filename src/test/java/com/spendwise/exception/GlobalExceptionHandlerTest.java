package com.spendwise.exception;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.exception.ExpenseException.ExpenseDoesNotExist;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.exception.WalletExceptions.DuplicateWalletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFoundException_shouldReturn404() {

        String message = "Expense does not exist";

        ResponseEntity<ErrorResponse> response =
                handler.handleExpenseDoesNotExist(
                        new ExpenseDoesNotExist(message)
                );

        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    @Test
    void handleBadRequestException_shouldReturn400() {

        String message = "Invalid pagination";

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidPaginationException(
                        new InvalidPaginationException(message)
                );

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @Test
    void handleBadCredentials_shouldReturn401() {

        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(
                        new org.springframework.security.authentication.BadCredentialsException(
                                "wrong credentials"
                        )
                );

        assertErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }

    @Test
    void handleForbiddenException_shouldReturn403() {

        String message = "Only group members can perform this action";

        ResponseEntity<ErrorResponse> response =
                handler.handleUnauthorizedGroupAction(
                        new UnauthorizedGroupActionException(message)
                );

        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                message
        );
    }

    @Test
    void handleConflictException_shouldReturn409() {

        String message = "Wallet already exists";

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateWalletException(
                        new DuplicateWalletException(message)
                );

        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                message
        );
    }

    @Test
    void handleMethodNotSupported_shouldReturn405() {

        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodNotSupported(exception);

        assertErrorResponse(
                response,
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method not supported for this endpoint"
        );
    }

    @Test
    void handleGenericException_shouldReturn500() {

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(
                        new RuntimeException("Something unexpected")
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    @Test
    void handleDataIntegrityViolation_shouldReturn409() {

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolation(
                        new DataIntegrityViolationException("duplicate key")
                );

        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                "The request conflicts with existing data"
        );
    }

    @Test
    void handleMalformedRequestBody_shouldReturn400() {

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Malformed JSON",
                        new HttpInputMessage() {

                            @Override
                            public InputStream getBody() {
                                return new ByteArrayInputStream(new byte[0]);
                            }

                            @Override
                            public HttpHeaders getHeaders() {
                                return new HttpHeaders();
                            }
                        }
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleHttpMessageNotReadable(exception);

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Malformed request body"
        );
    }

    @Test
    void handleInvalidParameterType_shouldReturn400() {

        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(
                        "abc",
                        UUID.class,
                        "userId",
                        null,
                        new IllegalArgumentException()
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentTypeMismatch(exception);

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: userId"
        );
    }

    @Test
    void handleMissingRequestParameter_shouldReturn400() {

        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "page",
                        "int"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleMissingServletRequestParameter(exception);

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: page"
        );
    }

    @Test
    void handleValidationException_shouldReturn400WithFieldErrors() {

        Object target = new Object();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "request");

        bindingResult.addError(
                new FieldError(
                        "request",
                        "name",
                        "Name is required"
                )
        );

        bindingResult.addError(
                new FieldError(
                        "request",
                        "email",
                        "Email must be valid"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        (MethodParameterFactory.parameter()),
                        bindingResult
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("Validation Failed", body.getMessage());
        assertNotNull(body.getTimestamp());

        assertNotNull(body.getErrors());
        assertEquals(2, body.getErrors().size());
        assertEquals("Name is required", body.getErrors().get("name"));
        assertEquals("Email must be valid", body.getErrors().get("email"));
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {

        assertEquals(expectedStatus, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);

        assertEquals(
                expectedStatus.value(),
                body.getStatus()
        );

        assertEquals(
                expectedStatus.getReasonPhrase(),
                body.getError()
        );

        assertEquals(
                expectedMessage,
                body.getMessage()
        );

        assertNotNull(body.getTimestamp());
    }

    /*
     * Small helper used only to construct MethodArgumentNotValidException.
     */
    private static class MethodParameterFactory {

        static org.springframework.core.MethodParameter parameter() {

            try {
                Method method =
                        MethodParameterFactory.class.getDeclaredMethod(
                                "dummy",
                                String.class
                        );

                return new org.springframework.core.MethodParameter(
                        method,
                        0
                );

            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unused")
        private static void dummy(String value) {
        }
    }
}