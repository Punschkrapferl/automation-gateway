package com.example.automationgateway.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void allArgsConstructorAndGettersWork() {
        DocumentRequest request = new DocumentRequest("some text");

        assertEquals("some text", request.getText());
    }

    @Test
    void noArgsConstructorAndSetterWork() {
        DocumentRequest request = new DocumentRequest();
        request.setText("hello");

        assertEquals("hello", request.getText());
    }

    @Test
    void notBlankValidationFailsOnNull() {
        DocumentRequest request = new DocumentRequest(null);

        Set<ConstraintViolation<DocumentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void notBlankValidationFailsOnEmptyString() {
        DocumentRequest request = new DocumentRequest("");

        Set<ConstraintViolation<DocumentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void notBlankValidationFailsOnWhitespaceOnly() {
        DocumentRequest request = new DocumentRequest("   ");

        Set<ConstraintViolation<DocumentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void notBlankValidationPassesOnNonEmptyText() {
        DocumentRequest request = new DocumentRequest("valid");

        Set<ConstraintViolation<DocumentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
