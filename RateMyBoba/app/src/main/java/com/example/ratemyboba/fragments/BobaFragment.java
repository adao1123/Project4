package com.example.ratemyboba.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.OnSwipeTouchListener;

import org.w3c.dom.Text;

import java.awt.font.TextAttribute;

/**
 * Created by adao1 on 5/2/2016.
 */
public class BobaFragment extends Fragment {
    private Tea tea;
    private TextView titleTV;
    private LinearLayout bobaLayout;
    private OnBobaSwipeRightListener swipeRightListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boba, container, false);
        titleTV = (TextView)view.findViewById(R.id.boba_title_id);
        bobaLayout = (LinearLayout)view.findViewById(R.id.boba_layout_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String bobaTitle = getArguments().getString("KEY");
        titleTV.setText(bobaTitle);
        setOnSwipeListener();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        swipeRightListener = (OnBobaSwipeRightListener)getActivity();
    }

    private void setOnSwipeListener(){
        bobaLayout.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            @Override
            public void onSwipeRight() {
                swipeRightListener.onBobaSwipeRight();
            }
        });
    }

    public interface OnBobaSwipeRightListener{
        void onBobaSwipeRight();
    }
}
