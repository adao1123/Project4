package com.example.ratemyboba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.models.Tea;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by adao1 on 5/2/2016.
 */
public class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.ViewHolder> {

    private List<Tea> mTeas;
    private final OnTeaClickListener listener;
    private Context context;


    public TeaAdapter(List<Tea> mTeas, OnTeaClickListener listener) {
        this.mTeas = mTeas;
        this.listener = listener;
    }

    public interface OnTeaClickListener{
        void onTeaClick(Tea tea);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View teaView = inflater.inflate(R.layout.rv_tea_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(teaView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tea tea = mTeas.get(position);
        TextView titleTV = holder.titleTV;
        titleTV.setText(tea.getTitle());
        ImageView imageView = holder.imageView;
        Picasso.with(context)
                .load(tea.getImageUrl())
                .into(imageView);
        holder.bind(mTeas.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return mTeas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView titleTV;
        public ImageView imageView;
//        public Button upButton;
//        public Button downButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView)itemView.findViewById(R.id.rv_tea_name);
            imageView = (ImageView)itemView.findViewById(R.id.rv_tea_image);
//            upButton = (Button)itemView.findViewById(R.id.rv_tea_plus);
//            downButton = (Button)itemView.findViewById(R.id.rv_tea_minus);
        }
        public void bind(final Tea tea, final OnTeaClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTeaClick(tea);
                }
            });
        }
    }
}
