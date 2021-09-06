package com.skynet.skynettest;

public class MessageModel {
    private String from, message, type, time, msgkey, to, name;

    public MessageModel(){}

    public MessageModel(String from, String message, String type, String time, String msgkey, String to) {
        this.to = to;
        this.msgkey = msgkey;
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public MessageModel(String from, String message, String type, String time, String msgkey, String to, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.msgkey = msgkey;
        this.to = to;
        this.name = name;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMsgkey() {
        return msgkey;
    }

    public void setMsgkey(String msgkey) {
        this.msgkey = msgkey;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
