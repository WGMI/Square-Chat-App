package com.example.mugandaimo.square;

/**
 * Created by Muganda Imo on 11/23/2018.
 */

public class Chat {

    public boolean seen;
    public long timestamp;

    public Chat(){

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Chat(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}