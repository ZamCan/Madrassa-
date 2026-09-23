package com.zamcan.madrassa.data.model;

public class LearningMaterial {
    public String id;
    public String programmeId;
    public String madrassaId;
    
    public String title;
    public String author;
    public String type; // e.g., PDF, LINK, TEXT, QURAN_REF
    public String content; // URL or text content
    public boolean active;
}
