package com.dao.ratemyboba.models;

/**
 * Created by adao1 on 5/9/2016.
 */
public class Review {
    private String user;
    private String userImage;
    private String text;
    private String rating;
    private String ratingImgUrl;

    public Review(String user, String userImage, String text, String rating) {
        this.user = user;
        this.userImage = userImage;
        this.text = text;
        this.rating = rating;
    }

    public Review() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRatingImgUrl() {
        return ratingImgUrl;
    }

    public void setRatingImgUrl(String ratingImgUrl) {
        this.ratingImgUrl = ratingImgUrl;
    }
}
