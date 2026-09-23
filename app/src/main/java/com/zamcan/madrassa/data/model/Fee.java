package com.zamcan.madrassa.data.model;

public class Fee {
    public String id;
    public String studentId;
    public String madrassaId;

    public String type;
    public double amount;

    public String deadline;
    public FeeStatus status;

    public long submittedAt;
    public long confirmedAt;
    public long lockedAt;

    public String submittedBy;
    public String confirmedBy;
}
