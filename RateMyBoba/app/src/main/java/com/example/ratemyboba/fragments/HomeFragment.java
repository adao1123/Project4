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
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adao1 on 5/1/2016.
 */
public class HomeFragment extends Fragment implements TeaAdapter.OnTeaClickListener{

    private static final String TAG = "HOME FRAGMENT";
    RecyclerView teaRV;
    List<Tea> teaList;
    PassClickedTeaListener teaListener;
    OnHomeFabClickListener fabListener;
    FloatingActionButton bobaFab;
    FloatingActionButton distanceFab;
    FloatingActionButton ratingsFab;
    FloatingActionButton dealsFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        teaRV = (RecyclerView)view.findViewById(R.id.home_RV_id);
        bobaFab = (FloatingActionButton)view.findViewById(R.id.home_fab_boba_id);
        distanceFab = (FloatingActionButton)view.findViewById(R.id.home_fab_distance_id);
        ratingsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_rating_id);
        dealsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_deals_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teaList = new ArrayList<>();
        fillList(); //TEMP/PLACEHOLDER
        setRV();
        setFabListener();
    }

    private void setRV(){
        TeaAdapter teaAdapter = new TeaAdapter(teaList,this);
        teaRV.setAdapter(teaAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(16);
        teaRV.addItemDecoration(decoration);
    }

    private void setFabListener(){
        bobaFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabListener.onHomeFabClick();
            }
        });
    }

    private void fillList(){
        for (int i = 1; i <= 25; i++){
            teaList.add(new Tea("Boba " + i));
        }
    }

    public interface OnHomeFabClickListener{
        void onHomeFabClick();
    }

    public interface PassClickedTeaListener{
        void passClickedTea(Tea tea);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        teaListener = (PassClickedTeaListener)getActivity();
        fabListener = (OnHomeFabClickListener)getActivity();
    }

    @Override
    public void onTeaClick(Tea tea) {
        teaListener.passClickedTea(tea);
    }
}
