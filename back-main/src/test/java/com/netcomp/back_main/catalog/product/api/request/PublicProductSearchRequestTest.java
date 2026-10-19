package com.netcomp.back_main.catalog.product.api.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PublicProductSearchRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void defaultValuesAreValid() {
        PublicProductSearchRequest request = new PublicProductSearchRequest(null, null, null);
        Set<ConstraintViolation<PublicProductSearchRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals(1, request.resolvedPage());
        assertEquals(12, request.resolvedPerPage());
    }

    @Test
    void validCustomParametersPassValidation() {
        PublicProductSearchRequest request = new PublicProductSearchRequest("asc", 2, 24);
        Set<ConstraintViolation<PublicProductSearchRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals(2, request.resolvedPage());
        assertEquals(24, request.resolvedPerPage());
    }

    @Test
    void invalidOrderFailsWithSpanishMessage() {
        PublicProductSearchRequest request = new PublicProductSearchRequest("invalid", 1, 12);
        Set<ConstraintViolation<PublicProductSearchRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("order debe ser asc o desc")));
    }

    @Test
    void invalidPageFailsWithSpanishMessage() {
        PublicProductSearchRequest request = new PublicProductSearchRequest("desc", 0, 12);
        Set<ConstraintViolation<PublicProductSearchRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("page debe ser mayor que cero")));
    }

    @Test
    void invalidPerPageFailsWithSpanishMessage() {
        PublicProductSearchRequest request = new PublicProductSearchRequest("desc", 1, 0);
        Set<ConstraintViolation<PublicProductSearchRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("per_page debe ser mayor que cero")));
    }
}
