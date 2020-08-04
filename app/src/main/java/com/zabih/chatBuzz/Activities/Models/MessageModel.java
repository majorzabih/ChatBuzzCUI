package com.zabih.chatBuzz.Activities.Models;

public class MessageModel {
    private String sender;
    private String receiver;
    private String message;
    private String date;
    private String time;
    private String username;
    private String image_url;
    private boolean isseen;

    public MessageModel(String sender, String receiver, String message, String date, String time, String username, String image_url, boolean isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.date = date;
        this.time = time;
        this.username = username;
        this.image_url = image_url;
        this.isseen = isseen;
    }

    public MessageModel() {
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
