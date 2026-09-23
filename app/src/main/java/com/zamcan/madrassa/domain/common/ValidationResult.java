package com.zamcan.madrassa.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects all validation failures instead of failing at
 * the first invalid field.
 */
public final class ValidationResult {

    private final List<String> errors =
            new ArrayList<>();

    public void add(String code) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        errors.add(code);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
