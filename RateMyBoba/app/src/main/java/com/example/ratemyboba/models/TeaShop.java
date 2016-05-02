package com.example.ratemyboba.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by adao1 on 5/2/2016.
 */
public class TeaShop {
    @SerializedName("id")
    private String teaShopId;

    public String getTeaShopId() {
        return teaShopId;
    }
}
