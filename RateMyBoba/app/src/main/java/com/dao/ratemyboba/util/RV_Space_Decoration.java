package com.dao.ratemyboba.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by adao1 on 5/2/2016.
 */

public class RV_Space_Decoration extends RecyclerView.ItemDecoration {
    private final int mSpace;
    public RV_Space_Decoration(int space) {
        this.mSpace = space;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0||parent.getChildAdapterPosition(view) == 1)
            outRect.top = mSpace;
    }
}