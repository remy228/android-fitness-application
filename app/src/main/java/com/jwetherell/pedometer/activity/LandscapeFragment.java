package com.jwetherell.pedometer.activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
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

    TextView AvgSpeed;
    TextView MinSpeed;
    TextView MaxSpeed;
    double maxdist=0;
    double mindist=0;
    double avgspeed=0;
    int prev_steps=0;
    int prev_cal=0;

    private Thread thread;

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
        AvgSpeed = (TextView) view.findViewById(R.id.textView22);
        MaxSpeed = (TextView) view.findViewById(R.id.textView29);
        MinSpeed = (TextView) view.findViewById(R.id.textView32);

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
        leftAxis.setAxisMaximum(30f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        //  feedMultiple();
        updateSpeed();
      //  s.start();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
                updateSpeed();
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
                        Thread.sleep(5000);
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

    double prev_dist=0;
    double current;
    //Function to update the Average, Min and Max speed values every 5 seconds
    private void updateSpeed() {

        double dist_current = Demo.dist;
        current = dist_current-prev_dist;
        prev_dist=dist_current;
        double maxspeed=0;
        double minspeed=0;



        if (current > maxdist) {
            maxdist = current;

        }

        if (maxdist>mindist) {
            mindist = prev_dist;
        }

        //Dividing 1/Distance covered in 1 minute.

        if(maxdist!=0) {
            maxspeed = 1 / maxdist;
        }
        double old_speed = maxspeed;
        if(mindist!=0) {
             minspeed = 1 / mindist;

            if(old_speed>minspeed)
                minspeed=old_speed;
                    }

        avgspeed=(maxspeed+minspeed)/2;

        String avg = String.format("%.1f", avgspeed);
        String max = String.format("%.1f", maxspeed);
        String min = String.format("%.1f", minspeed);

        AvgSpeed.setText(String.valueOf(avg));
        MaxSpeed.setText(String.valueOf(max));
        MinSpeed.setText(String.valueOf(min));

    }

//
   /*final Thread s = new Thread() {

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(5000);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {




                        }
                    });
                }
            } catch (InterruptedException e) {
            }
        }
    };
*/

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            if (set1 == null) {
                set1 = createSet1();
                data.addDataSet(set1);
            }


        //   data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
             int steps = Demo.Steps;
             int curr = steps-prev_steps;
             prev_steps=steps;

            int cal = Demo.calories_burnt;
            int curr_cal = cal-prev_cal;
            prev_cal = cal;

                data.addEntry(new Entry(set.getEntryCount(),curr), 0);
                data.addEntry(new Entry(set1.getEntryCount(), curr_cal), 1);


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


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Steps");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(4f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(32);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;

    }

    private LineDataSet createSet1() {

        LineDataSet set1 = new LineDataSet(null, "Calories");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.MAGENTA);
        set1.setCircleColor(Color.GREEN);
        set1.setLineWidth(4f);
        set1.setCircleRadius(4f);
      //set1.setFillAlpha(35);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTextSize(9f);
        set1.setDrawValues(false);
        return set1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.realtime, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

}
