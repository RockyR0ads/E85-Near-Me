package com.example.rocky.finde85now_001;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    /*
    ISSUES TO FIX
        - first time use crashes the app as we dont yet have permission to access location (possible fix: put app in pause state while user accepts location permission)

     */
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    Button click;
    Button stationsNearMe;
    Button firstStation;
    Button secondStation;
    Button thirdStation;
    TextView data;
    TextView textView;
    TextView errorCheck;
    TextView stateWatch;

    private double userLocationLongitude;
    private double userLocationLatitude;

    private ProgressBar progressBar;


    private static Boolean stopMapsLaunching = false;


    final static double homeLat = -33.926360;
    final static double homeLng = 151.121270;

    StationHandler stationHandler;
    Station station;
    Resources res;

    // GETTERS & SETTERS
    public double getUserLocationLatitude() {
        return userLocationLatitude;
    }

    public double getUserLocationLongitude() {
        return userLocationLongitude;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        click = findViewById(R.id.button);
        data = findViewById(R.id.fetchedData);
        textView = findViewById(R.id.textView);
        stationsNearMe = findViewById(R.id.stationsNearMe);
        firstStation = findViewById(R.id.firstStation);
        secondStation = findViewById(R.id.secondStation);
        thirdStation = findViewById(R.id.thirdStation);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        progressBar = findViewById(R.id.progressBar);
        errorCheck = findViewById(R.id.errorBoi);
        stateWatch = findViewById(R.id.state);

        stationHandler = new StationHandler();
        stationHandler.initialiseStations();
        station = new Station();
        res = getResources();

        stateWatch.setText(this.getLifecycle().getCurrentState().toString());

        getDeviceLocation();

        this.stateWatch.setText(this.getLifecycle().getCurrentState().toString());

        // FIND CLOSEST STATION
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMapsLaunching = false;
                HttpHandler asyncTask = new HttpHandler(MainActivity.this);
                asyncTask.execute();


            }
        });

        // FIND STATIONS NEAR ME
        stationsNearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMapsLaunching = true;
                HttpHandler asyncTask = new HttpHandler(MainActivity.this);
                progressBar.setVisibility(View.VISIBLE);
                animateProgressBar();
                asyncTask.execute();

            }
        });

        firstStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStationAddress());
            }
        });

        secondStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getSecondClosestStationAddress());
            }
        });

        thirdStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getThirdClosestStationAddress());
            }
        });
    }

    private void animateProgressBar() {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 1000);
        progressAnimator.setDuration(700);
        progressAnimator.setInterpolator(new AccelerateInterpolator());
        progressAnimator.start();
    }

    private void getDeviceLocation() {

        try {
            getLocationPermission();

            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {

                                    userLocationLatitude = location.getLatitude();
                                    userLocationLongitude = location.getLongitude();

                                    data.setText("Longitude: " + userLocationLongitude + "\nLatitude: " + userLocationLatitude);

                                    stationHandler.getDistanceBetween(userLocationLatitude, userLocationLongitude);
                                } else {
                                    data.setText("Location is null.");
                                }
                            }

                        });

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         *
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                getDeviceLocation();
                // permission was granted, yay! Do the things
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                textView.setText(" This \n application \n requires \n location \n permissions \n to run");
            }
        }
    }

    private void launchMaps(String station) {
        String format = "google.navigation:q=" + station; // setup the string to pass
        Uri uri = Uri.parse(format); // parse it into a format maps can read
        Intent launchMap = new Intent(Intent.ACTION_VIEW, uri);

        launchMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // do i need this?
        //  launchMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchMap.setPackage("com.google.android.apps.maps"); // choose the google maps app
        this.startActivity(launchMap);
    }

    private void buildDialog(){

        Station s = stationHandler.getStationByAddress(station.getClosestStationAddress());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(s.getCompany() + " " + s.getSuburb() + " is CLOSED");
        builder.setMessage("Navigate to the closest open station?");
        builder.setIcon(android.R.drawable.ic_dialog_alert)


                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String openStation = stationHandler.findOpenStation();
                        launchMaps(openStation);

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();

                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog builder1 = builder.create();
        builder1.show();
    }

    private static class HttpHandler extends AsyncTask<Void, Integer, String> {

        private WeakReference<MainActivity> activityWeakReference;
        // only retain a weak reference to the activity
        HttpHandler(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... voids) {

            MainActivity activity = activityWeakReference.get();
            String locationString = StationHandler.getLocationsToSend();

            try {

                URL testingParsedDestination = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + activity.getUserLocationLatitude() + "," + activity.getUserLocationLongitude() + "&destinations=" + locationString + "&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");

                //URL hardCodedTest = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + lat + "," + lng + "&destinations=-33.901877,151.037178&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");

                HttpURLConnection httpURLConnection = (HttpURLConnection) testingParsedDestination.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                String data = "";

                while (line != null) {
                    line = bufferedReader.readLine();
                    data = data + line;
                }

                //Parse the data in a readable manner

                JSONObject JO = new JSONObject(data);

                String checkRequest = JO.getString("status");

                Log.d("checkInvalidLog", "Checking INVALID REQUEST");

                if (checkRequest.equals("INVALID_REQUEST")) {
                    Log.d("checkInvalidLog1", "INVALID REQUEST");
                    return "TEST";
                }

                JSONArray rowsArray = JO.getJSONArray("rows");
                JSONArray destAddresses = JO.getJSONArray("destination_addresses");
                JSONObject row0 = (JSONObject) rowsArray.get(0);
                JSONArray elements = row0.getJSONArray("elements");

                String test = rowsArray.toString();



                for (int i = 0; i < elements.length(); ++i) {

                    JSONObject objects = elements.getJSONObject(i);

                    activity.stationHandler.timeToArriveInTraffic.add(objects.getJSONObject("duration_in_traffic").getInt("value"));
                    activity.stationHandler.distanceToStation.add(objects.getJSONObject("distance").getString("text"));
                    activity.stationHandler.timeToStation.add(objects.getJSONObject("duration_in_traffic").getString("text"));
                    activity.stationHandler.addressesReturned.add(destAddresses.getString(i));


                }


                ArrayList<Integer> minutesToDestination = new ArrayList<>(activity.stationHandler.timeToArriveInTraffic);

                for (String d : activity.stationHandler.addressesReturned) {
                    Log.d("checkDestinationStrings", d);
                }

                Collections.sort(minutesToDestination);

                // THIS NEEDS AN ARRAY IMPLEMENTATION FOR CLOSEST STATIONS

//                for(int i= 0; i < minutesToDestination.size(); i++) {
//
//                    activity.station.setClosestStationAddress((destAddresses.getString(activity.stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(i)))));
//
//                }

                activity.station.setClosestStationAddress((destAddresses.getString(activity.stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(0)))));
                activity.station.setSecondClosestStationAddress((destAddresses.getString(activity.stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(1)))));
                activity.station.setThirdClosestStationAddress((destAddresses.getString(activity.stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(2)))));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return activity.station.getClosestStationAddress();
        }


        @Override
        protected void onPostExecute(String output) {

            MainActivity activity = activityWeakReference.get();
            activity.progressBar.setVisibility(View.GONE);
            Drawable red = activity.res.getDrawable(R.drawable.btn_rounded_red);
            Drawable green = activity.res.getDrawable(R.drawable.btn_rounded_green);

            if (output != null && stopMapsLaunching) { // user wants to see the 3 closest stations
                super.onPostExecute(output);

                // modify the activity's UI
                if(activity.stationHandler.getStationByAddress(activity.station.getClosestStationAddress()).isTheStationOpen()){
                    activity.firstStation.setBackground(green);
                }else{activity.firstStation.setBackground(red);}

                if(activity.stationHandler.getStationByAddress(activity.station.getSecondClosestStationAddress()).isTheStationOpen()){
                    activity.secondStation.setBackground(green);
                }else{activity.secondStation.setBackground(red);}

                if(activity.stationHandler.getStationByAddress(activity.station.getThirdClosestStationAddress()).isTheStationOpen()){
                    activity.thirdStation.setBackground(green);
                }else{activity.thirdStation.setBackground(red);}

                activity.firstStation.setVisibility(View.VISIBLE);
                activity.firstStation.setText(activity.stationHandler.snmStringConstruct(activity.station.getClosestStationAddress()));
                activity.secondStation.setVisibility(View.VISIBLE);
                activity.secondStation.setText(activity.stationHandler.snmStringConstruct(activity.station.getSecondClosestStationAddress()));
                activity.thirdStation.setVisibility(View.VISIBLE);
                activity.thirdStation.setText(activity.stationHandler.snmStringConstruct(activity.station.getThirdClosestStationAddress()));

                activity.stateWatch.setText("state:" + activity.getLifecycle().getCurrentState().toString());

            } else {
                    if (activity.stationHandler.getStationByAddress(activity.station.getClosestStationAddress()).isTheStationOpen()) { // station is open send the user to maps
                        activity.launchMaps(activity.station.getClosestStationAddress());
                        activity.finish();

                    } else { // station is closed
                            activity.buildDialog();
                            activity.errorCheck.setText("Station is not open Go EAT ASS");
                            activity.stateWatch.setText("state:" + activity.getLifecycle().getCurrentState().toString());

                    }
            }
        }
    }
}
