package com.example.firebase;

import java.io.Serializable;

public class Video1Model implements Serializable {
    private String videoId;
    private String userId; // Chỉ lưu userId để tham chiếu
    private String videoUrl;
    private String title;
    private String description;
    private long likes;
    private long dislikes;

    // Constructor đầy đủ
    public Video1Model(String videoId, String userId, String videoUrl, String title,
                       String description, long likes, long dislikes) {
        this.videoId = videoId;
        this.userId = userId;
        this.videoUrl = videoUrl;
        this.title = title;
        this.description = description;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    // Constructor mặc định cho Firebase
    public Video1Model() {
    }

    // Getter và Setter
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }
}