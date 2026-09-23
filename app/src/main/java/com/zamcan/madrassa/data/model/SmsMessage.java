package com.zamcan.madrassa.data.model;

public class SmsMessage {
    public String id;
    public String madrassaId;

    public String recipient;
    public String message;

    public String type;
    public String provider;

    public SmsStatus status;

    public long createdAt;
    public long sentAt;
    public long deliveredAt;
}
