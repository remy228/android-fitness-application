package com.jwetherell.pedometer.activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import  android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jwetherell.pedometer.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class LandscapeFragment extends android.app.Fragment {

 //   Intent recieveIntent = getActivity().getIntent();

    private LineChart mChart;

    public LandscapeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = inflater.inflate(R.layout.fragment_landscape, container, false);
        mChart = (LineChart) view.findViewById(R.id.lineChart);
 //       mChart.setOnChartValueSelectedListener(this);

      //  mChart.setDescription();
        mChart.setNoDataText("No data for now");

        // enable description text
       mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        //enable value highlighting
        mChart.setHighlightPerDragEnabled(true);
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(mTfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
       // xl.setTypeface(mTfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
       // leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
     //  feedMultiple();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
             //   updateSpeed();
            }
        };



        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {

                    // Don't generate garbage runnables inside the loop.
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(runnable);

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    private void updateSpeed() {

     //   com.jwetherell.pedometer.activity.Demo.storeData();
        /*recieveIntent = new Intent(getActivity().getApplicationContext(), Demo.class);
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            int steps = extras.getInt("Steps");
            double distance = extras.getDouble("Distance");
            int calories = extras.getInt("Calories");
            System.out.println("Intent test for real time chart: " + steps + " " + distance + " " + calories);

        }*/
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }

    }

    //Retrieving data for Calories and Steps every 5 minutes
    private void getData()
    {

    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.realtime, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

     /*  switch (item.getItemId()) {
            case R.id.actionAdd: {
                addEntry();
                break;
            }
            case R.id.actionClear: {
                mChart.clearValues();
                Toast.makeText(getActivity(), "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionFeedMultiple: {
                feedMultiple();
                break;
            }
        }*/
        return true;
    }


    private Thread thread;

  /* private void feedMultiple() {

        if (thread != null)
            thread.interrupt();



    }

    @Override
    public void onValueSelected(Entry e, Highlight h)
        {
            Log.i("Entry selected", e.toString());
        }


    @Override
    public void onNothingSelected()  {
        Log.i("Nothing selected", "Nothing selected.");
        feedMultiple();
    }*/


}
