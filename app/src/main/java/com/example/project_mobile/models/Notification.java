package com.example.project_mobile.models;

public class Notification {
    private String title;
    private String body;
    private String time;

    public Notification() {}

    public Notification(String title, String body, String time) {
        this.title = title;
        this.body = body;
        this.time = time;
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
}