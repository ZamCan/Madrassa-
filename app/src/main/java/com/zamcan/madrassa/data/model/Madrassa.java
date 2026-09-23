package com.zamcan.madrassa.data.model;

public class Madrassa {
    public String id;
    public String name;
    public String type;
    public String administrationType;

    public String region;
    public String district;
    public String ward;
    public String area;
    public String nearbyLandmark;

    public String masjidName;
    public String masjidLocation;

    public String headUstadhId;
    public String phone;
    public String secondaryPhone;
    public String email;

    public int ustadhCount;

    public ApprovalStatus approvalStatus;
    public String rejectionReason;
    public long submittedAt;
    public long approvedAt;
}
