package com.example.ratemyboba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.models.Tea;

import java.util.List;

/**
 * Created by adao1 on 5/2/2016.
 */
public class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.ViewHolder> {

    private List<Tea> mTeas;
    private final OnTeaClickListener listener;

    public TeaAdapter(List<Tea> mTeas, OnTeaClickListener listener) {
        this.mTeas = mTeas;
        this.listener = listener;
    }

    public interface OnTeaClickListener{
        void onTeaClick(Tea tea);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View teaView = inflater.inflate(R.layout.rv_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(teaView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tea tea = mTeas.get(position);
        TextView titleTV = holder.titleTV;
        titleTV.setText(tea.getTitle());
        holder.bind(mTeas.get(position),listener);
    }

    @Override
    public int getItemCount() {
        return mTeas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView titleTV;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView)itemView.findViewById(R.id.rv_title_id);
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
