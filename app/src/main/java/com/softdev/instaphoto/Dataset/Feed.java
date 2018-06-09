package com.softdev.instaphoto.Dataset;

public class Feed {
    private String user_id;
    private String username;
    private String name;
    private String icon;
    private String postId;
    private String type;
    private String content;
    private String description;
    private String creation;
    private boolean isLiked;
    public int likes;
    private String comments;
    private boolean isCommentsEnable;
    private boolean isPublicCommentsAllowed;
    private boolean isFollowing;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLikes() {
        return String.valueOf(likes);
    }

    public void setLikes(String likes) {
        this.likes = Integer.parseInt(likes);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean isCommentsEnable() {
        return isCommentsEnable;
    }

    public void setCommentsEnable(boolean commentsEnable) {
        isCommentsEnable = commentsEnable;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public boolean isPublicCommentsAllowed() {
        return isPublicCommentsAllowed;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public void setPublicCommentsAllowed(boolean publicCommentsAllowed) {
        isPublicCommentsAllowed = publicCommentsAllowed;
    }
}
