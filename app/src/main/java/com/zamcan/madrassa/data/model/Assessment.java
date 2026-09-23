package com.zamcan.madrassa.data.model;

public class Assessment {
    public String id;
    public String madrassaId;
    public String studentId;
    public String assignmentId;
    public String programmeId;
    
    public double score;
    public String feedback;
    public ProgressStatus status;
    public long recordedAt;
    public String recordedBy;
}
