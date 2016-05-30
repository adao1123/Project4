package com.dao.ratemyboba.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dao.ratemyboba.R;

/**
 * Created by adao1 on 5/23/2016.
 * View Holder for Reviews recycler view that holds textviews to display review and rating
 */

public class ReviewsViewHolder extends RecyclerView.ViewHolder {
    public TextView reviewUserTV;
    public TextView reviewBodyTV;
    public TextView reviewRatingTV;
    public ImageView reviewUserIV;

    public ReviewsViewHolder(View itemView) {
        super(itemView);
        reviewUserTV = (TextView)itemView.findViewById(R.id.rv_review_user);
        reviewBodyTV = (TextView) itemView.findViewById(R.id.rv_review_body);
        reviewRatingTV = (TextView)itemView.findViewById(R.id.rv_review_rating);
        reviewUserIV = (ImageView)itemView.findViewById(R.id.rv_review_user_image);
    }
}
