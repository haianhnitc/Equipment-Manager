package com.example.project_mobile.models;

public class Notification {
    private String title;
    private String body;
    private String time;
    private String messageId;
    public Notification() {};

    public Notification(String title, String body, String time, String messageId) {
        this.title = title;
        this.body = body;
        this.time = time;
        this.messageId = messageId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }
    public String getMessageId() {
        return messageId;
    }


}