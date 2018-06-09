package com.softdev.instaphoto.Dataset;

public class Like {

    private String postId;
    private String username;
    private String name;
    private String creation;
    private String icon;
    private boolean isFollowed = false;

    public String getCreation() {
        return creation;
    }

    public String getName() {
        return name;
    }

    public String getPostid() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getIcon() {
        return icon;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPostid(String postId) {
        this.postId = postId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isFollowed() {
        return isFollowed;
    }
}
