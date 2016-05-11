package com.example.ratemyboba.models;

/**
 * Created by adao1 on 5/2/2016.
 */
public class Tea {
    private String title;
    private String imageUrl;
    private int numUp;
    private int numDown;
    private int points;


    public Tea() {
    }

    public Tea(String title) {
        this.title = title;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Tea(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.numUp = 0;
        this.numDown = 0;
        this.points = this.numUp - this.numDown;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getNumUp() {
        return numUp;
    }

    public int getNumDown() {
        return numDown;
    }

    public void setNumUp(int numUp) {
        this.numUp = numUp;
    }

    public void setNumDown(int numDown) {
        this.numDown = numDown;
    }
}
