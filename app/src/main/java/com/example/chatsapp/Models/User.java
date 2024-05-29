package com.example.chatsapp.Models;

public class User {

    private String uid, name, phoneNumber, profileImage, fcmToken;
    private long unreadCount;
    private long lastMsgTime;

    public User() {


    }

    public User(String uid, String name, String phoneNumber, String profileImage, String fcmToken) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.fcmToken = fcmToken;
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

    public long getUnreadCount() {
        return unreadCount;

    }

    public long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(long timeDate) {
        this.lastMsgTime = timeDate;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;

    }
}