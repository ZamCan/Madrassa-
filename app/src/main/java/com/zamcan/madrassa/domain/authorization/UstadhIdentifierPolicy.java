package com.zamcan.madrassa.domain.authorization;

public final class UstadhIdentifierPolicy {

    private UstadhIdentifierPolicy() {
    }

    public static boolean isBlank(
            String identifier
    ) {
        return identifier == null ||
                identifier.trim().isEmpty();
    }

    public static boolean looksLikePhone(
            String identifier
    ) {
        if (isBlank(identifier)) {
            return false;
        }

        String value =
                identifier.trim();

        boolean hasDigit = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (Character.isDigit(c)) {
                hasDigit = true;
                continue;
            }

            if (c == '+' ||
                    c == ' ' ||
                    c == '-' ||
                    c == '(' ||
                    c == ')' ||
                    c == '.') {
                continue;
            }

            return false;
        }

        return hasDigit;
    }

    public static String normalizeName(
            String identifier
    ) {
        if (isBlank(identifier)) {
            return "";
        }

        return identifier
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }
}
