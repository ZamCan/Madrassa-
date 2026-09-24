package com.zamcan.madrassa.data.model;

public enum ProgressStatus {
    NOT_STARTED,

    /** @deprecated legacy database alias; normalize reads to IN_PROGRESS. */
    @Deprecated
    STARTED,

    IN_PROGRESS,
    COMPLETED,
    REVIEWING,
    NEEDS_REVIEW
}
