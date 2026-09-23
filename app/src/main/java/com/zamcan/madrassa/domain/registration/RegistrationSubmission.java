package com.zamcan.madrassa.domain.registration;

import com.zamcan.madrassa.data.model.Madrassa;

public final class RegistrationSubmission {

    public final Madrassa madrassa;

    /*
     * Temporary plaintext password supplied by the
     * registration UI.
     *
     * It must never be stored in Madrassa or any
     * persistent model. RegistrationService hashes
     * it immediately.
     */
    public final String initialPassword;

    public RegistrationSubmission(
            Madrassa madrassa,
            String initialPassword
    ) {
        this.madrassa = madrassa;
        this.initialPassword = initialPassword;
    }
}
