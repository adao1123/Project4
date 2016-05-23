package com.example.ratemyboba.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ratemyboba.R;

/**
 * Created by adao1 on 5/23/2016.
 */

public class BobaViewHolder extends RecyclerView.ViewHolder {
    public TextView titleTV;
    public TextView pointsTV;
    public ImageView imageView;
    public Button upButton;
    public Button downButton;

    public BobaViewHolder(View itemView) {
        super(itemView);
        titleTV = (TextView)itemView.findViewById(R.id.rv_tea_name);
        pointsTV = (TextView)itemView.findViewById(R.id.rv_tea_points);
        imageView = (ImageView)itemView.findViewById(R.id.rv_tea_image);
        upButton = (Button)itemView.findViewById(R.id.rv_tea_plus);
        downButton = (Button)itemView.findViewById(R.id.rv_tea_minus);
    }
}
