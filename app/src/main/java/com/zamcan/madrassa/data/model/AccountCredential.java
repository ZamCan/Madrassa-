package com.zamcan.madrassa.data.model;

public class AccountCredential {

    public String id;

    /*
     * The authenticated account.
     *
     * Ustadh -> Ustadh.id
     * Parent -> Parent.id
     * Admin  -> administrator account id
     */
    public String accountId;

    public AccountRole role;

    /*
     * Tenant boundary.
     *
     * Critical for authorization and future backend enforcement.
     */
    public String madrassaId;

    /*
     * Password material.
     *
     * Never store plaintext passwords.
     */
    public String passwordHash;
    public String passwordSalt;

    public boolean active;
    public boolean firstLogin;

    public long createdAt;
    public long updatedAt;
    public long lastLoginAt;
}
