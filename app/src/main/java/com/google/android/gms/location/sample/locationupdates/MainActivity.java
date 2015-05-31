/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdates;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "location-updates-sample";
    public final static String EXTRA_MESSAGE = "mainActivity.MESSAGE";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;



    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    private double previousLongitude;
    private double localLongtitude;
    private double previousLatitude;
    private double localLatitude;
    private static double totalDistance = 0.0;
    private static double compDistance = 0.0;
    protected double timeSingleValue;


    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mMoveTextView;
    protected TextView mTotalDistanceTextView;
    protected TextView mSpeedTextView;
    protected LineChartView mLineChart;
    protected TextView mCalTextView;
    protected TextView mCalPredictTextView;


    private Date previousDate = new Date();
    private Date localDate = new Date();

    private static ArrayList<Long> timeslots = new ArrayList<Long>();
    private static ArrayList<Float> userspeeds = new ArrayList<Float>();
    private static ArrayList<Float> compspeeds = new ArrayList<Float>();


    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    protected String initialTime;
    protected double weight;
    protected double distance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Bundle bundle = getIntent().getExtras();
        //weight = Double.parseDouble(bundle.getString("EXTRA_WEIGHT"));
        //distance = Double.parseDouble(bundle.getString("EXTRA_DISTANCE"));


        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mMoveTextView = (TextView) findViewById(R.id.moveTextView);
        mTotalDistanceTextView = (TextView) findViewById(R.id.totalDistanceTextView);
        mSpeedTextView = (TextView) findViewById(R.id.speed_text);
        mLineChart = (LineChartView) findViewById(R.id.linechart);
        mCalTextView = (TextView) findViewById(R.id.cal_value);
        mCalPredictTextView = (TextView) findViewById(R.id.cal_predict_val);



        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        Date date = new Date();

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        /*
        String[] mLabels = {"ANT", "GNU", "OWL", "APE", "COD","YAK", "RAM", "JAY"};
        LineChartView mLineChart = (LineChartView) findViewById(R.id.linechart);
        LineSet data1 = new LineSet();
        LineSet data2 = new LineSet();
        float dataset1[] = {0.2f, 3.4f, 1.2f,4.3f,2.3f,4.3f,2.3f,3.1f };
        float dataset2[] = {0.4f, 1.4f, 2.2f,4.3f,3.3f,4.4f,1.3f,2.1f };
        data1.addPoints(mLabels, dataset1);
        data2.addPoints(mLabels, dataset2);
        //int[] colors1 = {Color.parseColor("#3388c6c3"), Color.TRANSPARENT};
        data1.setLineColor(Color.parseColor("#3388c6c3"));
        //int[] colors2 = {Color.parseColor("#1133c6c3"), Color.TRANSPARENT};
        data2.setLineColor(Color.parseColor("#663313c3"));
        mLineChart.addData(data1);
        mLineChart.addData(data2);
        mLineChart.show();
        */
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLastUpdateTime);

            localLatitude = Double.parseDouble(mLatitudeTextView.getText().toString());
            localLongtitude = Double.parseDouble(mLongitudeTextView.getText().toString());
            double newDistance = Math.round(gps2m(previousLatitude, previousLongitude, localLatitude, localLongtitude) * 100) / 100.0;


            if (!mStartUpdatesButton.isEnabled()) {
                mMoveTextView.setText(String.valueOf(newDistance) + " m");
                totalDistance += newDistance;
                double totalDistanceOutput = Math.round(totalDistance * 100) / 100.0;
                totalDistanceOutput = totalDistanceOutput < 0 ? 0 : totalDistanceOutput;
                mTotalDistanceTextView.setText(String.valueOf(totalDistanceOutput) + "m");
            }
            previousLatitude = localLatitude != 0 ? localLatitude : previousLongitude;
            previousLongitude = localLongtitude != 0 ? localLongtitude : previousLatitude;

            if (!mStartUpdatesButton.isEnabled()) {
                double speed = Double.parseDouble(timeToSpeed());

                timeslots.add(System.currentTimeMillis());
                userspeeds.add((float) speed);
                float randomspeed = (float)Math.random()*6;
                compspeeds.add(randomspeed);
                mSpeedTextView.setText(String.valueOf(speed) + " km/h");

                double calValue = calorieCalculator(weight, timeSingleValue / 3600, speed);
                calValue = calValue < 0 ? 0 : calValue;
                calValue = calValue > 100 ? 100 : calValue;
                calValue = calValue / 1000.0 * 60;
                calValue = Math.round(calValue * 100) / 100.0;
                mCalTextView.setText(String.valueOf(calValue) + " kCal/m" );
                compDistance += 5*randomspeed/3600*1000;
                double predictedCalories = (calValue / timeSingleValue) * 60;
                predictedCalories = (predictedCalories < 0 || predictedCalories > 100000) ? 0 : predictedCalories;
                mCalPredictTextView.setText("~ " + String.valueOf(predictedCalories) + " kCal");

                if (compDistance > totalDistance)
                    System.out.println("be faster!");
                /*
                //String[] mLabels = {"ANT", "GNU", "OWL", "APE", "COD","YAK", "RAM", "JAY"};
                LineChartView mLineChart = (LineChartView) findViewById(R.id.linechart);
                LineSet data1 = new LineSet();
                LineSet data2 = new LineSet();
                float[] dataset1 = new float[speedlist.length];
                String[] mLabels = new String[speedlist.length];
                //float dataset2[speedlist.length];
                for (int i=0; i<speedlist.length; i++) {
                    dataset1[i] = speedlist[i];
                    mLabels[i] = "";
                }
                data1.addPoints(mLabels, dataset1);
                //data2.addPoints(mLabels, dataset2);
                //int[] colors1 = {Color.parseColor("#3388c6c3"), Color.TRANSPARENT};
                data1.setLineColor(Color.parseColor("#3388c6c3"));
                //int[] colors2 = {Color.parseColor("#1133c6c3"), Color.TRANSPARENT};
                //data2.setLineColor(Color.parseColor("#663313c3"));


                if (speedlist.length > 1) {
                    mLineChart.updateValues(1, data1);
                    //mLineChart.addData(data2);
                    mLineChart.show();
                }
                */

            }
        }
    }

    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (double) (180/3.14169);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
        double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
        double t3 = Math.sin(a1)*Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000*tt;
    }

    /**
     * Calculates time difference between measurements and speed of the object
     */

    protected String timeToSpeed() {

        String speedValueText = "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa");

            localDate = dateFormat.parse(mLastUpdateTimeTextView.getText().toString());

            long localDateMS = localDate.getTime();
            long previousDateMs = previousDate.getTime();
            double timeValueSecond = (localDateMS - previousDateMs) / 1000;

            timeSingleValue = timeValueSecond;

            double distance = Double.parseDouble(mMoveTextView.getText().toString().substring(0, mMoveTextView.length() - 2));
            double speedValue = Math.round(((distance / timeValueSecond) * 3600 / 1000)*100)/100.0;
            if (speedValue < 0.01 || Double.isNaN(speedValue) || speedValue > 10000) speedValue = 0;

            speedValueText =  String.valueOf(speedValue);

            previousDate = localDate;
        }
        catch (ParseException e) {
            mSpeedTextView.setText(e.getMessage());
        }

        return speedValueText ; //  String.valueOf(timeValueMS);
    }


    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {

        mLineChart = (LineChartView) findViewById(R.id.linechart);

        /*String[] mLabels = {"ANT", "GNU", "OWL", "APE", "COD","YAK", "RAM", "JAY"};
        LineSet data1 = new LineSet();
        LineSet data2 = new LineSet();
        float dataset1[] = {0.2f, 3.4f, 1.2f,4.3f,2.3f,4.3f,2.3f,3.1f };
        float dataset2[] = {0.4f, 1.4f, 2.2f,4.3f,3.3f,4.4f,1.3f,2.1f };
        data1.addPoints(mLabels, dataset1);
        data2.addPoints(mLabels, dataset2);
        //int[] colors1 = {Color.parseColor("#3388c6c3"), Color.TRANSPARENT};
        data1.setLineColor(Color.parseColor("#3388c6c3"));
        //int[] colors2 = {Color.parseColor("#1133c6c3"), Color.TRANSPARENT};
        data2.setLineColor(Color.parseColor("#663313c3"));
        mLineChart.addData(data1);
        mLineChart.addData(data2);
        mLineChart.show();*/
        LineChartView mLineChart = (LineChartView) findViewById(R.id.linechart);
        LineSet data1 = new LineSet();
        LineSet data2 = new LineSet();

        Float[] speedlist = userspeeds.toArray(new Float[userspeeds.size()]);
        Float[] compspeedlist = compspeeds.toArray(new Float[userspeeds.size()]);
        Long[] timelist = timeslots.toArray(new Long[timeslots.size()]);
        System.out.println(speedlist);
        System.out.println(timelist);

        float[] dataset1 = new float[speedlist.length];
        float[] dataset2 = new float[compspeedlist.length];
        String[] mLabels = new String[speedlist.length];
        //float dataset2[speedlist.length];
        for (int i=0; i<speedlist.length; i++) {
            dataset1[i] = speedlist[i];
            mLabels[i] = "";
        }
        data1.addPoints(mLabels, dataset1);
        data2.addPoints(mLabels, dataset2);
        //int[] colors1 = {Color.parseColor("#3388c6c3"), Color.TRANSPARENT};
        data1.setLineColor(Color.parseColor("#3388c6c3"));
        //int[] colors2 = {Color.parseColor("#1133c6c3"), Color.TRANSPARENT};
        data2.setLineColor(Color.parseColor("#663313c3"));



        if (speedlist.length > 1) {
            mLineChart.addData(data1);
            mLineChart.addData(data2);
            mLineChart.show();
        }


        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Calculate the carlorie
     * @param weight (unit: kg)
     * @param hours (unit: h)
     * @param speed (unit: km/h)
     * @return calorie
     */
    public double calorieCalculator(double weight, double hours, double speed) {
        double K = 30 / (speed * 3.0 / 8.0);
        return weight * hours * K;
    }

}
