package com.softdev.instaphoto.Dataset;

public class Comment {
    private String postId;
    private String username;
    private String name;
    private String content;
    private String creation;
    private String icon;
    private String commentId;

    public String getContent() {
        return content;
    }

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

    public void setContent(String content) {
        this.content = content;
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

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
