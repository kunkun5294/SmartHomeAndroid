package edu.temple.m.smarthomedroid;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import edu.temple.m.smarthomedroid.Handlers.HttpHandler;

/**
 * Created by M on 4/23/2017.
 */

public class SensorGraphFragment extends Fragment {
    private final String TAG = "SensorGraphFragment";
    Bundle bundle;

    private String sessionToken;
    private String houseName;
    private String peripheralName;
    private JSONObject jsonObject;
    private JSONArray dataArray;

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;
    Date startTime;
    Date endTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //Create new view from Sensor fragment layout
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        bundle = new Bundle();
        sessionToken = getArguments().getString("SessionToken");
        houseName = getArguments().getString("HouseName");
        peripheralName = getArguments().getString("PeripheralName");

        jsonObject = new JSONObject();
        try{
            jsonObject.put("SessionToken", sessionToken);
            jsonObject.put("HouseName", houseName);
            jsonObject.put("PeripheralName", peripheralName);
        } catch(JSONException e){
            e.printStackTrace();
        }
        Log.d("SensorGraphFragment", jsonObject.toString());
        boolean dataIsValid = false;
        try {
            dataIsValid = new PopulateData().execute(jsonObject).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Log.d(TAG, houseName + " " + peripheralName);
        if (dataIsValid) {
            Log.d("SensorGraphFragment", "dataIsValid == true");
            GraphView graph = (GraphView) view.findViewById(R.id.sensor_graph);
            DataPoint[] data = generateData();
            mSeries1 = new LineGraphSeries<>(data);
            graph.addSeries(mSeries1);
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
            graph.getViewport().setMinX(data[0].getX());
            graph.getViewport().setMaxX(data[data.length-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

            graph.getGridLabelRenderer().setHumanRounding(false);
        } else {
            Log.d("SensorGraphFragment", "dataIsValid == false");
        }

//        mSeries2.resetData(generateData());

        return view;
    }
/*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
    }
*/
    @Override
    public void onResume() {
        super.onResume();
        /*
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                mSeries1.resetData(generateData());
                mHandler.postDelayed(this, 10000);
            }
        };
        mHandler.postDelayed(mTimer1, 1000);
        */
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }

    private DataPoint[] generateData() {
        ArrayList<String> time = new ArrayList<>();
        ArrayList<Integer> data = new ArrayList<>();
        JSONObject tempJson = new JSONObject();
        int xTime, yData;
        Date date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DataPoint[] values;

        if(dataArray != null){
            Log.d("generateData", "dataArray != null");
            int length = dataArray.length();
            values = new DataPoint[length];
            for(int i = 0; i < length; i++){
                try {
                    tempJson = dataArray.getJSONObject(i);
                    Log.d(TAG, tempJson.toString());
                    time.add(tempJson.getString("TimeStamp"));
                    data.add(tempJson.getInt("PeripheralValue"));
                    Timestamp ts = Timestamp.valueOf(time.get(i));
                    date = new Date(ts.getTime());
                    yData = tempJson.getInt("PeripheralValue");

                    DataPoint temp = new DataPoint(date, yData);
                    values[i] = temp;

                    if(i == 0){
                        startTime = date;
                    }
                    endTime = date;
                } catch(JSONException e){
                    e.printStackTrace();
                }
            }
            return values;
        }

        return null;
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }

    private class PopulateData extends AsyncTask<JSONObject, Void, Boolean>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(JSONObject...args){

            String response = new HttpHandler().makePostCall("https://zvgalu45ka.execute-api.us-east-1.amazonaws.com/prod/gethistoricdata", args[0]);

            Log.d("SensorGraphFragment", response);

            if(response != null){
                try {
                    dataArray=new JSONArray(response);
                    dataArray=dataArray.getJSONArray(0);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
        }
    }
}
