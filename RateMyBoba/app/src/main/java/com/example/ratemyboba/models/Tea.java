package com.example.ratemyboba.models;

/**
 * Created by adao1 on 5/2/2016.
 */
public class Tea {
    private String title;
    private String imageUrl;
    private int numUp;
    private int numDown;


    public Tea() {
    }

    public Tea(String title) {
        this.title = title;
    }

    public Tea(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
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
}
