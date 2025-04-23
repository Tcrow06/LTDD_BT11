package com.example.firebase;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String userId; // Thay uid bằng userId
    private String email;
    private String username;
    private String profileImageUrl;

    // Constructor đầy đủ
    public UserProfile(String userId, String email, String username, String profileImageUrl) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    // Constructor mặc định cho Firebase
    public UserProfile() {
    }

    // Getter và Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}