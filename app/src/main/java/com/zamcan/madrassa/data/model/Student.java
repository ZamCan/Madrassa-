package com.zamcan.madrassa.data.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    public String id;
    public String madrassaId;
    public String parentId;

    public String fullName;
    public String parentGuardianName;

    public String mainPhone;
    public String emergencyPhone;

    public String classId;

    public String quranLevel;
    public String hifzLevel;

    public List<String> programmeIds = new ArrayList<>();

    public boolean active;
}
