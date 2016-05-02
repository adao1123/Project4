package com.example.ratemyboba.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.models.Tea;

import org.w3c.dom.Text;

import java.awt.font.TextAttribute;

/**
 * Created by adao1 on 5/2/2016.
 */
public class BobaFragment extends Fragment {
    private Tea tea;
    private TextView titleTV;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boba,container,false);
        titleTV = (TextView)view.findViewById(R.id.boba_title_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String bobaTitle = getArguments().getString("KEY");
        titleTV.setText(bobaTitle);
    }
}
