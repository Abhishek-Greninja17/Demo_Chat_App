package com.skynet.skynettest;

public class GroupChatModel {

    private String message, type, from, time, msgKey, fromName, fileName;

    public GroupChatModel(){}

    public GroupChatModel(String message, String type, String from, String time, String msgKey, String fromName) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.time = time;
        this.msgKey = msgKey;
        this.fromName = fromName;
    }

    public GroupChatModel(String message, String type, String from, String time, String msgKey, String fromName, String fileName) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.time = time;
        this.msgKey = msgKey;
        this.fromName = fromName;
        this.fileName = fileName;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
