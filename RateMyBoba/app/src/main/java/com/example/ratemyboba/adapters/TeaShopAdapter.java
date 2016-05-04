package com.example.ratemyboba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.models.TeaShop;
import com.example.ratemyboba.models.TeaShopList;
import com.squareup.picasso.Picasso;
import com.yelp.clientlib.entities.Business;

import java.util.List;

/**
 * Created by adao1 on 5/2/2016.
 */
public class TeaShopAdapter extends RecyclerView.Adapter<TeaShopAdapter.ViewHolder> {

    private static final String TAG = "TEA SHOP ADAPTER";
    private List<Business> mTeaShops;
    private final OnTeaShopClickListener listener;
    private Context context;


    public TeaShopAdapter(List<Business> mTeaShops, OnTeaShopClickListener listener) {
        this.mTeaShops = mTeaShops;
        this.listener = listener;
    }

    public interface OnTeaShopClickListener{
        void onTeaShopClick(Business teaShop);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        Log.i(TAG, "onCreateViewHolder: ");
        LayoutInflater inflater = LayoutInflater.from(context);
        View teaView = inflater.inflate(R.layout.rv_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(teaView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Business teaShop = mTeaShops.get(position);
        TextView titleTV = holder.titleTV;
        ImageView teaIV = holder.teaIV;
        titleTV.setText(teaShop.name());
        Log.i(TAG, "onBindViewHolder: "+teaShop.imageUrl());

        Picasso.with(context)
                .load(teaShop.imageUrl().replaceAll("ms", "348s"))
                //.resizeDimen(200,200)
                .resize(200, 200)
                .into(teaIV);
        holder.bind(mTeaShops.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return mTeaShops.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView titleTV;
        public ImageView teaIV;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView)itemView.findViewById(R.id.rv_title_id);
            teaIV = (ImageView)itemView.findViewById(R.id.rv_image_id);
        }
        public void bind(final Business teaShop, final OnTeaShopClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTeaShopClick(teaShop);
                }
            });
        }
    }
}