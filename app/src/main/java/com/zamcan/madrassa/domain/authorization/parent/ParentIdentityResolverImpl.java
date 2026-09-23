package com.zamcan.madrassa.domain.authorization.parent;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.repository.ParentStore;

public final class ParentIdentityResolverImpl
        implements ParentIdentityResolver {

    private final ParentStore parentStore;

    public ParentIdentityResolverImpl(ParentStore parentStore) {
        if (parentStore == null) {
            throw new IllegalArgumentException("parentStore is required");
        }

        this.parentStore = parentStore;
    }

    @Override
    public Parent findByPhone(String phone) {
        String normalized = normalizePhone(phone);

        if (normalized == null) {
            return null;
        }

        return parentStore.findByPhone(normalized);
    }

    @Override
    public Parent resolve(String identifier) {
        return findByPhone(identifier);
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String value = phone.trim();

        if (value.isEmpty()) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                result.append(c);
            } else if (c == '+'
                    || c == ' '
                    || c == '-'
                    || c == '('
                    || c == ')'
                    || c == '.') {
                if (c == '+') {
                    if (result.length() == 0) {
                        result.append(c);
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }

        if (result.length() == 0) {
            return null;
        }

        String normalized = result.toString();

        if (normalized.startsWith("00")) {
            normalized = "+" + normalized.substring(2);
        }

        /*
         * Single national format: 0712345678, 255712345678 and
         * +255712345678 all resolve to +255712345678, so accounts
         * stored before the +255 prefix UI still match.
         */
        if (normalized.startsWith("+255")) {
            return normalized;
        }

        if (normalized.startsWith("255")
                && normalized.length() == 12) {
            return "+" + normalized;
        }

        if (normalized.startsWith("0")
                && normalized.length() == 10) {
            return "+255" + normalized.substring(1);
        }

        return normalized;
    }
}
