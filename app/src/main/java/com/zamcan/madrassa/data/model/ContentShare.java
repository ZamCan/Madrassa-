package com.zamcan.madrassa.data.model;

public class ContentShare {

    public static final String CONTENT_LESSON = "LESSON";
    public static final String CONTENT_MATERIAL = "MATERIAL";
    public static final String CONTENT_ASSIGNMENT = "ASSIGNMENT";

    public static final String SCOPE_MADRASSA = "MADRASSA";
    public static final String SCOPE_STUDENT = "STUDENT";
    public static final String SCOPE_GROUP = "GROUP";

    public String id;

    /*
     * The tenant that owns the access/share record.
     * This is NOT a duplicate of the canonical content.
     */
    public String madrassaId;

    /*
     * Canonical content reference.
     */
    public String contentType;
    public String contentId;

    /*
     * MADRASSA = all students in madrassa
     * STUDENT  = one student
     * GROUP    = a student group
     */
    public String scope;
    public String targetId;

    /*
     * Ustadh/user who created the share.
     */
    public String createdBy;

    public boolean active;

    public long createdAt;
    public long updatedAt;
}
