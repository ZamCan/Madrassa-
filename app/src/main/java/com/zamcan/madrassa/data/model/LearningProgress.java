package com.zamcan.madrassa.data.model;

public class LearningProgress {
    public String id;
    public String learnerId; // Student or Solo Learner ID
    public String lessonId;
    public String madrassaId; // Nullable for Solo
    public ProgressStatus status;
    public long updatedAt;
}
