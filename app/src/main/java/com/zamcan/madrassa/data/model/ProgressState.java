package com.zamcan.madrassa.data.model;

/** @deprecated Use ProgressStatus; retained as a source-compatible bridge. */
@Deprecated
public final class ProgressState {
    public static final ProgressStatus NOT_STARTED = ProgressStatus.NOT_STARTED;
    public static final ProgressStatus STARTED = ProgressStatus.IN_PROGRESS;
    public static final ProgressStatus IN_PROGRESS = ProgressStatus.IN_PROGRESS;
    public static final ProgressStatus COMPLETED = ProgressStatus.COMPLETED;
    public static final ProgressStatus REVIEWING = ProgressStatus.REVIEWING;
    public static final ProgressStatus NEEDS_REVIEW = ProgressStatus.NEEDS_REVIEW;
    private ProgressState() { }
}
