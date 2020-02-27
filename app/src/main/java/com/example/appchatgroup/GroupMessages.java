package com.example.appchatgroup;

public class GroupMessages {
    private String name, message,from;

    public GroupMessages(){

    }

    public GroupMessages(String message, String name, String from) {
        this.message = message;
        this.name = name;
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setFrom() {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
