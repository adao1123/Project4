package com.dao.ratemyboba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dao.ratemyboba.R;
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
    private final OnEndOfListListener endOfListListener;
    private Context context;
    private double latitude,longitude;

    public TeaShopAdapter(List<Business> mTeaShops, OnTeaShopClickListener listener, OnEndOfListListener onEndOfListListener, double latitude, double longitude) {
        this.mTeaShops = mTeaShops;
        this.listener = listener;
        this.endOfListListener = onEndOfListListener;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public interface OnTeaShopClickListener{
        void onTeaShopClick(Business teaShop);
    }

    public interface OnEndOfListListener{
        void onEndOfList(int position);
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
        TextView addressTV = holder.addressTV;
        TextView distanceTV = holder.distanceTV;
        double distance = Math.round(getDistance(latitude,longitude,
                teaShop.location().coordinate().latitude(),
                teaShop.location().coordinate().longitude()) * 100.0) / 100.0;
        distanceTV.setText(distance+"m");
        if (latitude==0)distanceTV.setText("");
        ImageView teaIV = holder.teaIV;
        ImageView ratingIV = holder.ratingIV;
        titleTV.setText(teaShop.name());
        String address = teaShop.location().displayAddress().toString();
        addressTV.setText(address.substring(1,address.length()-1));
        Picasso.with(context)
                .load(teaShop.imageUrl().replaceAll("ms", "348s"))
                .into(teaIV);
        Picasso.with(context)
                .load(teaShop.ratingImgUrlLarge())
                .into(ratingIV);
        holder.bind(mTeaShops.get(position), listener);
        if (position==mTeaShops.size()-3){
            endOfListListener.onEndOfList(position);
        }
    }

    public void setLocation(double lat, double lon){
        latitude=lat;
        longitude=lon;
    }

    @Override
    public int getItemCount() {
        return mTeaShops.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView titleTV;
        public TextView addressTV;
        public TextView distanceTV;
        public ImageView ratingIV;
        public ImageView teaIV;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView)itemView.findViewById(R.id.rv_title_id);
            addressTV = (TextView)itemView.findViewById(R.id.rv_address_id);
            distanceTV = (TextView)itemView.findViewById(R.id.rv_distance_id);
            teaIV = (ImageView)itemView.findViewById(R.id.rv_image_id);
            ratingIV = (ImageView)itemView.findViewById(R.id.rv_rating_id);
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


    public static double getDistance(double lat1, double lon1, double lat2, double lon2){ //returns distance from two latlon pairs
        int R = 6378100; //radius of the EARTH in meters
        float latDistance = toRad(lat2-lat1);
        float lonDistance = toRad(lon2 - lon1);
        float a = (float) (Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
        double miles = R *c/1609.34;
        Log.i(TAG, "getDistance: miles"+miles);
        return miles;//return miles
    }
    private static float toRad(double value) {
        return (float) (value * Math.PI / 180); //made this as a float alternative to Math.toRadians
    }

}