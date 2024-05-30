package com.example.chatsapp.Models;

public class User {

    private String uid, name, phoneNumber, profileImage, fcmToken;

    private  long timeAndDate;

    private long lastMsgTime; // Add this field

    // Existing constructors and methods

    public long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }


    public User() {


    }

    public User(String uid, String name, String phoneNumber, String profileImage, String fcmToken) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.fcmToken = fcmToken;
    }

    public long getTimeAndDate() {
        return timeAndDate;
    }

    public void setTimeAndDate(long timeAndDate) {
        this.timeAndDate = timeAndDate;
    }





    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}