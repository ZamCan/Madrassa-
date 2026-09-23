package com.zamcan.madrassa.data.model;

import java.util.ArrayList;
import java.util.List;

public class Parent {

    public String id;
    public String madrassaId;

    public String fullName;
    public String phone;

    public boolean firstLogin;
    public boolean active;

    public List<String> linkedStudentIds = new ArrayList<>();
}
