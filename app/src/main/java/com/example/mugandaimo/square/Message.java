package com.example.mugandaimo.square;

/**
 * Created by Muganda Imo on 11/16/2018.
 */

public class Message {

    String message,type,sender;
    boolean seen;
    long time;

    public Message(){

    }

    public Message(String message, String type, String sender, boolean seen, long time) {
        this.message = message;
        this.type = type;
        this.sender = sender;
        this.seen = seen;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
