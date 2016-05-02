package com.example.ratemyboba.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ratemyboba.R;
import com.example.ratemyboba.TeaAdapter;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Divider_Decoration;
import com.example.ratemyboba.util.RV_Space_Decoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adao1 on 5/1/2016.
 */
public class HomeFragment extends Fragment implements TeaAdapter.OnTeaClickListener{
    RecyclerView teaRV;
    List<Tea> teaList;
    PassClickedTeaListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        teaRV = (RecyclerView)view.findViewById(R.id.home_RV_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teaList = new ArrayList<>();
        fillList(); //TEMP/PLACEHOLDER
        setRV();
    }

    private void setRV(){
        TeaAdapter teaAdapter = new TeaAdapter(teaList,this);
        teaRV.setAdapter(teaAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(16);
        teaRV.addItemDecoration(decoration);
    }

    private void fillList(){
        for (int i = 1; i <= 25; i++){
            teaList.add(new Tea("Boba " + i));
        }
    }

    public interface PassClickedTeaListener{
        void passClickedTea(Tea tea);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (PassClickedTeaListener)getActivity();
    }

    @Override
    public void onTeaClick(Tea tea) {
        listener.passClickedTea(tea);
    }
}
