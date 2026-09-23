package com.zamcan.madrassa.data.model;

public class PendingRegistrationCredential {

    public String id;

    /*
     * The Madrassa awaiting approval.
     */
    public String madrassaId;

    /*
     * The account role that will be provisioned
     * after approval.
     */
    public AccountRole role;

    /*
     * Password material is stored only as a
     * salted password hash.
     *
     * NEVER store the plaintext registration password.
     */
    public String passwordHash;
    public String passwordSalt;

    public long createdAt;
    public long updatedAt;
}
