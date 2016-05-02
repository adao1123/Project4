package com.example.ratemyboba;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ratemyboba.fragments.BobaFragment;
import com.example.ratemyboba.fragments.HomeFragment;
import com.example.ratemyboba.models.Tea;

public class MainActivity extends AppCompatActivity implements HomeFragment.PassClickedTeaListener, BobaFragment.OnBobaSwipeRightListener{

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragmentManager();
    }

    private void setFragmentManager(){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        fragmentTransaction.add(R.id.fragment_contatinerID, homeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void passClickedTea(Tea tea) {
        fragmentTransaction = fragmentManager.beginTransaction();
        BobaFragment bobaFragment = new BobaFragment();
        Bundle bundle = new Bundle();
        bundle.putString("KEY",tea.getTitle());
        bobaFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_contatinerID, bobaFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onBobaSwipeRight() {
        fragmentTransaction = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        fragmentTransaction.replace(R.id.fragment_contatinerID, homeFragment);
        fragmentTransaction.commit();
    }
}
