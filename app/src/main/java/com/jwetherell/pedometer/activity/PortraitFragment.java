package com.jwetherell.pedometer.activity;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jwetherell.pedometer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PortraitFragment extends android.app.Fragment {


    public PortraitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_portrait, container, false);


    }

}
