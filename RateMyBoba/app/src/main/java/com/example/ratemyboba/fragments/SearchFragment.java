package com.example.ratemyboba.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ratemyboba.R;
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.adapters.TeaShopAdapter;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adao1 on 5/2/2016.
 */
public class SearchFragment extends Fragment implements TeaShopAdapter.OnTeaShopClickListener{

    private static final String TAG = "SEARCH FRAGMENT";
    private ArrayList<Business> teaShopList;
    private RecyclerView teaRV;
    private TeaShopAdapter teaShopAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
        teaRV = (RecyclerView)view.findViewById(R.id.search_RV_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setRV();
        setYelpApi();
    }

    private void setYelpApi(){
        Log.i(TAG, "setYelpApi: inside");
        YelpAPIFactory yelpAPIFactory = new YelpAPIFactory(
                getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET),
                getString(R.string.YELP_TOKEN_KEY),getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = yelpAPIFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        //params.put("category_filter","Bubble Tea");
        params.put("term", "Boba");
        Call<SearchResponse> call = yelpAPI.search("Milpitas",params);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                teaShopList = response.body().businesses();
                for (Business teaShop : teaShopList) {
                    Log.i(TAG, "onResponse: " + teaShop.name());
                }
                //teaShopAdapter.notifyDataSetChanged();
                setRV();
                //teaShopAdapter.notifyItemRangeInserted(0,teaShopList.size()-1);
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void setRV(){
        //teaShopList = new ArrayList<>();
        teaShopAdapter = new TeaShopAdapter(teaShopList,this);
        teaRV.setAdapter(teaShopAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(16);
        teaRV.addItemDecoration(decoration);
    }


    @Override
    public void onTeaShopClick(Business teaShop) {

    }
}
