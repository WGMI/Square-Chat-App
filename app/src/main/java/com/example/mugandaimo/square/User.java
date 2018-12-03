package com.example.mugandaimo.square;

/**
 * Created by Muganda Imo on 8/16/2018.
 */

public class User {

    String username,status,image,compressed_image;
    boolean online;

    public User(){

    }

    public User(String username, String status, String image,String compressed_image,boolean online) {
        this.username = username;
        this.status = status;
        this.image = image;
        this.compressed_image = compressed_image;
        this.online = online;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCompressed_image() {
        return compressed_image;
    }

    public void setCompressed_image(String compressed_image) {
        this.compressed_image = compressed_image;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
