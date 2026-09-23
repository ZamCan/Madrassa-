package com.zamcan.madrassa.core.validation;

import com.zamcan.madrassa.data.model.FeeStatus;

public final class EduNoorRules {

    public static final String DEFAULT_DIAL_CODE = "+255";

    public static final int MIN_PARENT_PASSWORD_LENGTH = 6;
    public static final int MIN_USTADH_PASSWORD_LENGTH = 6;

    public static final long FEE_CORRECTION_WINDOW_MS =
            30L * 60L * 1000L;

    private EduNoorRules() {
    }

    public static boolean validParentPassword(String password) {
        if (password == null || password.length() < MIN_PARENT_PASSWORD_LENGTH) {
            return false;
        }

        boolean letter = false;
        boolean number = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                number = true;
            }
        }

        return letter && number;
    }

    public static boolean validUstadhPassword(String password) {
        if (password == null || password.length() < MIN_USTADH_PASSWORD_LENGTH) {
            return false;
        }

        boolean letter = false;
        boolean number = false;
        boolean special = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                number = true;
            } else {
                special = true;
            }
        }

        return letter && number && special;
    }

    /*
     * Password feedback for registration UI.
     *
     * Returns the codes of the requirements that are still
     * missing, in fix order: "length", "letter", "number",
     * "special". Empty means the Ustadh password is valid.
     * A code maps to password_missing_<code> strings, where the
     * special-character message shows examples such as , % @ #.
     */
    public static java.util.List<String> missingUstadhPasswordParts(
            String password
    ) {
        java.util.List<String> missing =
                new java.util.ArrayList<>();

        if (password == null || password.isEmpty()) {
            missing.add("length");
            missing.add("letter");
            missing.add("number");
            missing.add("special");
            return missing;
        }

        if (password.length() < MIN_USTADH_PASSWORD_LENGTH) {
            missing.add("length");
        }

        boolean letter = false;
        boolean number = false;
        boolean special = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                number = true;
            } else {
                special = true;
            }
        }

        if (!letter) {
            missing.add("letter");
        }

        if (!number) {
            missing.add("number");
        }

        if (!special) {
            missing.add("special");
        }

        return missing;
    }

    /*
     * 0 = empty, 1 = weak, 2 = fair, 3 = strong.
     * Maps to password_strength_weak/fair/strong strings.
     */
    public static int ustadhPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        if (password.length() >= MIN_USTADH_PASSWORD_LENGTH) {
            score++;
        }

        if (password.length() >= 10) {
            score++;
        }

        boolean letter = false;
        boolean number = false;
        boolean special = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                number = true;
            } else {
                special = true;
            }
        }

        int kinds = 0;

        if (letter) {
            kinds++;
        }

        if (number) {
            kinds++;
        }

        if (special) {
            kinds++;
        }

        if (kinds >= 2) {
            score++;
        }

        if (kinds == 3 && password.length() >= 8) {
            score++;
        }

        if (score <= 1) {
            return 1;
        }

        if (score <= 3) {
            return 2;
        }

        return 3;
    }

    /*
     * Phone handling for an international dialling prefix.
     *
     * "+255" is the product default; the registration form can
     * switch the prefix per country (+254, +256, +20) from the
     * geography catalog. Accepts the national format with or
     * without the trunk zero ("0712 345 678"), the national
     * format with the code ("255 712 345 678") or the full
     * international format ("+255 712 345 678") and normalises
     * to "<dial> <national>".
     *
     * Registration and login both normalise, so older accounts
     * stored without the code still match.
     */
    public static String normalizePhone(
            String dialCode,
            String raw
    ) {

        String dial =
                dialCode == null || dialCode.trim().isEmpty()
                        ? DEFAULT_DIAL_CODE
                        : dialCode.trim();

        if (!dial.startsWith("+")) {
            dial = "+" + dial;
        }

        String digits =
                raw == null ? "" : raw.replaceAll("[^0-9]", "");

        String dialDigits = dial.substring(1);

        if (digits.startsWith(dialDigits)) {
            digits = digits.substring(dialDigits.length());
        } else if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        int nationalLength = nationalLength(dial);

        if (digits.length() > nationalLength) {
            digits = digits.substring(
                    digits.length() - nationalLength
            );
        }

        return dial + " " + digits;
    }

    public static boolean validPhone(
            String dialCode,
            String raw
    ) {

        String dial =
                dialCode == null || dialCode.trim().isEmpty()
                        ? DEFAULT_DIAL_CODE
                        : dialCode.trim();

        if (!dial.startsWith("+")) {
            dial = "+" + dial;
        }

        String normalised = normalizePhone(dial, raw);

        return normalised.matches(
                java.util.regex.Pattern.quote(dial)
                        + " [0-9]{"
                        + nationalLength(dial)
                        + "}"
        );
    }

    /*
     * National significant number length per dialling prefix.
     * Unlisted prefixes assume the common 9-digit pattern; the
     * catalog only ships TZ/KE/UG/EG today.
     */
    private static int nationalLength(String dial) {

        if ("+20".equals(dial)) {
            return 10;
        }

        return 9;
    }

    /*
     * Tanzanian phone handling for the +255 country prefix.
     * Kept as the named default used by authentication, where
     * the account always belongs to a Tanzanian number.
     */
    public static String normalizeTanzanianPhone(String raw) {
        return normalizePhone(DEFAULT_DIAL_CODE, raw);
    }

    public static boolean validTanzanianPhone(String raw) {
        return validPhone(DEFAULT_DIAL_CODE, raw);
    }

    /*
     * Minimal offline email sanity check for registration:
     * one @, text on both sides, and a dotted domain.
     * Kept dependency-free; full RFC validation is out of
     * scope for an offline-first form.
     */
    public static boolean validEmail(String raw) {
        if (raw == null) {
            return false;
        }

        String value = raw.trim();

        if (value.length() < 5 || value.length() > 254) {
            return false;
        }

        int at = value.indexOf('@');

        if (at <= 0
                || at != value.lastIndexOf('@')
                || at == value.length() - 1) {
            return false;
        }

        String domain = value.substring(at + 1);

        if (!domain.contains(".")) {
            return false;
        }

        if (domain.startsWith(".")
                || domain.endsWith(".")) {
            return false;
        }

        for (char c : value.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean feeCanBeCorrected(
            FeeStatus status,
            long submittedAt,
            long now
    ) {
        if (status != FeeStatus.PENDING) {
            return false;
        }

        return now - submittedAt <= FEE_CORRECTION_WINDOW_MS;
    }

    public static boolean feeCanBeConfirmed(FeeStatus status) {
        return status == FeeStatus.PENDING;
    }
}
