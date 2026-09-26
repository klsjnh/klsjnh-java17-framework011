package com.klsjnh.web.global;

/*                GlobalExceptionHandlerValidationTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  @Valid mapping onto envelope 400
 *
 */

import com.klsjnh.common.enums.HttpCodeEnum011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for validation exception mapping in {@link GlobalExceptionHandler}.
 */

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerValidationTest {

    @Mock
    private RuntimeStatusPort runtimeStatusPort;

    private GlobalExceptionHandler handler;

    /**
     * Create the handler under test.
     */
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(runtimeStatusPort);
    }

    /**
     * MethodArgumentNotValidException maps to HTTP 400 with the first field message.
     *
     * @throws Exception when building the exception
     */
    @Test
    void methodArgumentNotValidMapsToBadRequestEnvelope() throws Exception {
        when(runtimeStatusPort.isDebug()).thenReturn(false);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "vo");
        bindingResult.addError(new FieldError("vo", "code", "code is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Response011<Void>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpCodeEnum011.BAD_REQUEST.getCode(), response.getStatusCode().value());
        assertEquals(HttpCodeEnum011.BAD_REQUEST.getCode(), response.getBody().getStatusCode());
        assertEquals("code is required", response.getBody().getMessage());
        assertEquals("", response.getBody().getErrorMessage());
    }

    /**
     * MissingServletRequestParameterException maps to HTTP 400.
     */
    @Test
    void missingRequestParameterMapsToBadRequestEnvelope() {
        when(runtimeStatusPort.isDebug()).thenReturn(false);

        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("objectName",
                "String");

        ResponseEntity<Response011<Void>> response = handler.handleMissingParameter(ex);

        assertEquals(HttpCodeEnum011.BAD_REQUEST.getCode(), response.getStatusCode().value());
        assertEquals("objectName is required", response.getBody().getMessage());
    }

    /**
     * MultipartException maps to HTTP 400.
     */
    @Test
    void multipartExceptionMapsToBadRequestEnvelope() {
        when(runtimeStatusPort.isDebug()).thenReturn(false);

        MultipartException ex = new MultipartException("no multipart boundary");

        ResponseEntity<Response011<Void>> response = handler.handleMultipart(ex);

        assertEquals(HttpCodeEnum011.BAD_REQUEST.getCode(), response.getStatusCode().value());
        assertEquals("multipart request is invalid", response.getBody().getMessage());
    }

    /**
     * Debug mode fills errorMessage with the validation detail.
     *
     * @throws Exception when building the exception
     */
    @Test
    void debugModeExposesErrorMessage() throws Exception {
        when(runtimeStatusPort.isDebug()).thenReturn(true);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "vo");
        bindingResult.addError(new FieldError("vo", "code", "code is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Response011<Void>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals("code is required", response.getBody().getMessage());
        assertEquals(true, response.getBody().getErrorMessage() != null
                && response.getBody().getErrorMessage().contains("code is required"));
    }
}
