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

class DocumentRequestDTOTest {

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
        DocumentRequestDTO request = new DocumentRequestDTO("some text");

        assertEquals("some text", request.getText());
    }

    @Test
    void noArgsConstructorAndSetterWork() {
        DocumentRequestDTO request = new DocumentRequestDTO();
        request.setText("hello");

        assertEquals("hello", request.getText());
    }

    @Test
    void notBlankValidationFailsOnNull() {
        DocumentRequestDTO request = new DocumentRequestDTO(null);

        Set<ConstraintViolation<DocumentRequestDTO>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void notBlankValidationFailsOnEmptyString() {
        DocumentRequestDTO request = new DocumentRequestDTO("");

        Set<ConstraintViolation<DocumentRequestDTO>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void notBlankValidationFailsOnWhitespaceOnly() {
        DocumentRequestDTO request = new DocumentRequestDTO("   ");

        Set<ConstraintViolation<DocumentRequestDTO>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void notBlankValidationPassesOnNonEmptyText() {
        DocumentRequestDTO request = new DocumentRequestDTO("valid");

        Set<ConstraintViolation<DocumentRequestDTO>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
