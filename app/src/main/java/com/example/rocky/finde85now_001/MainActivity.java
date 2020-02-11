package com.example.rocky.finde85now_001;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


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
    HttpHandler HH;
    TextView data;
    TextView textView;

    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private double userLocationLongitude;
    private double userLocationLatitude;

    private ProgressBar progressBar;

    private static String locationsToSend = "";

    public static Boolean getStopMapsLaunching() {
        return stopMapsLaunching;
    }

    private static Boolean stopMapsLaunching = false;

    final static double homeLat = -33.926360;
    final static double homeLng = 151.121270;

    DateFormat dateFormatter = new SimpleDateFormat("kk:mm", Locale.ENGLISH);

    //Arrays to hold station lists
    double[] distance = new double[24];
    double[] storedStations = new double[22];
    float[] straightLineDistanceInMeters = new float[1];
    boolean[] checkIfStationIsOpen = new boolean[11];

    private ArrayList<String> possibleDest = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

    //GETTERS & SETTERS

    public static String getLocationsToSend() {
        return locationsToSend;
    }

    public double getUserLocationLatitude() {

        return userLocationLatitude;
    }

    public double getUserLocationLongitude() {
        return userLocationLongitude;
    }

    Station vineyard = new Station(-33.649917,150.862685,530,2130);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        HH = new HttpHandler(this);

        stations.add(vineyard);
        stations.add(new Station (-33.810202,151.032491,600,2200));

        //Testing if my class works
        String s = String.valueOf(vineyard.getClosingTime());
        vineyard.setFullAddress("1540 Windsor Road, Vineyard, NSW 2765, Australia");
        Log.d("VineyardCloseTime", s); // result = 2130 (SUCCESS)


            // store all SYDNEY United stations in array

            storedStations[0] = -33.649917; // vineyard
            storedStations[1] = 150.862685;
            checkIfStationIsOpen[0] = false;

            storedStations[2] = -33.901877; // yagoona
            storedStations[3] = 151.037178;

            storedStations[4] = -33.810202; // rydalmere
            storedStations[5] = 151.032491;

            storedStations[6] = -33.755790; // Dee Why
            storedStations[7] = 151.282715;

            storedStations[8] = -33.746039; // east Blaxland
            storedStations[9] = 150.622454;

            storedStations[10] = -33.861967; // Rozelle
            storedStations[11] = 151.167653;

            storedStations[12] = -33.901910; // waterloo
            storedStations[13] = 151.208229;

            storedStations[14] = -33.899258; // yagoona1
            storedStations[15] = 151.036924;

            storedStations[16] = -33.872234; // prairewood
            storedStations[17] = 150.900077;

            storedStations[18] = -34.030073; // minto
            storedStations[19] = 150.831892;

            storedStations[20] = -33.680160; // Terrey Hills
            storedStations[21] = 151.225010;

        // Caltex Station currently removed due to bug

//            storedStations[22] = -33.856990; // Drummoyne
//            storedStations[23] = 151.146040;

       // createDateRange(dateTest1 , dateTest2);

        click = findViewById(R.id.button);
        data = findViewById(R.id.fetchedData);
        textView = findViewById(R.id.textView);
        stationsNearMe = findViewById(R.id.stationsNearMe);
        firstStation = findViewById(R.id.firstStation);
        secondStation = findViewById(R.id.secondStation);
        thirdStation = findViewById(R.id.thirdStation);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        progressBar = findViewById(R.id.progressBar);

        getDeviceLocation();


        // FIND CLOSEST STATION
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopMapsLaunching = false;
                HH.execute();
                finish();

            }
        });

        // FIND STATIONS NEAR ME
        stationsNearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopMapsLaunching = true;
                progressBar.setVisibility(View.VISIBLE);
                startAnimation();
                HH.execute();

            }
        });

        firstStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchMaps(HH.closestE85Address);

            }
        });

        secondStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchMaps(HH.secondClosestStation);

            }
        });

        thirdStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchMaps(HH.thirdClosestStation);

            }
        });

    }

    private boolean isNowBetweenDateTime(int opening, int closing)
    {
        String currentTime;
        int timeNow;

        //get current user time
        currentTime = new SimpleDateFormat("kk:mm", Locale.getDefault()).format(new Date());
        Log.d("theCurrentTime", currentTime); // check if its working
        timeNow = Integer.valueOf(currentTime);

        if (timeNow > opening && timeNow < closing) {
            return false;
        } else {
            return false;
        }

    }

    private void startAnimation(){

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 1000);
        progressAnimator.setDuration(700);
        progressAnimator.setInterpolator(new AccelerateInterpolator());
        progressAnimator.start();
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // Check for PERMISSION
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                       // onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void getLastLocation() {

        // initialise an FLPC object
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

            // Check if permission is not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);


            return;
        }

        // Get last known recent location using new Google Play Services SDK (v11+)

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {

                            onLocationChanged(location);

                            userLocationLongitude = 151.049502;
                            userLocationLatitude = -33.830092;

                            data.setText("Longitute: " + userLocationLongitude + "\nLatitude: " + userLocationLatitude);
                            getDistanceBetween();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });

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
                                    // Logic to handle location object

                                   // userLocationLongitude = location.getLongitude();
                                   // userLocationLatitude = location.getLatitude();

//                                    userLocationLongitude = 151.049502; // silverwater test
//                                    userLocationLatitude = -33.830092;

//                                    userLocationLongitude = 151.1442;  // brighton le sands
//                                    userLocationLatitude = -33.9627;

//                                    userLocationLatitude = -33.867970;
//                                    userLocationLongitude = 151.128870;  // five dock

                                      userLocationLatitude = -33.653588;
                                    userLocationLongitude = 150.868142;  // vineyard

                                    data.setText("Longitute: " + userLocationLongitude + "\nLatitude: " + userLocationLatitude);

                                    getDistanceBetween();
                                    stringConstructor();
                                }
                                else{data.setText("location is null");}
                            }

                        });

            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
     //   deviceLng = location.getLongitude();
      //  deviceLat = location.getLatitude();
        data.setText("Longitute: " + homeLng + "\nLatitude: " + homeLat);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
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

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // call this once the device pos has been received
    private void getDistanceBetween() {

        for(int i = 0; i < storedStations.length; i++ ){

            Location.distanceBetween(storedStations[i], storedStations[i+1], userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);

            distance[i] = straightLineDistanceInMeters[0];

            // store sub 30km stations in a straight line
            if(straightLineDistanceInMeters[0] < 30000){

                possibleDest.add(storedStations[i]+"");
                possibleDest.add(storedStations[i+1]+"");
            }

            String distanceInStraightLine = Double.toString(distance[i]);
            Log.d("distanceInStraightLine", distanceInStraightLine);

            i+=1;
        }

    }

    // create the string of coordinates to be send in the HTTPS request based of the closest stations decided in getDistanceBetween

    private void stringConstructor(){

        int size = 0;
        int testSize = 0;
        int i = 0;

        StringBuilder sb = new StringBuilder();
        size = possibleDest.size();
        testSize = size-1;

        for (String d : possibleDest) {
            //String stationsWithinRange = Double.toString(d);

            if(i % 2 == 0){
                sb.append(d + ",");
            }
            else if(i!=testSize){

                sb.append(d + "|");
            }
            if(i==testSize) {
                sb.append(d);
            }

            i++;
            Log.d("stationsWithinRange", d); // testing the straight line distances in meters for all stations in syd

        }
        locationsToSend = sb.toString();

        Log.d("locationsToSend", locationsToSend);


    }

    private void launchMaps(String station){


        boolean isIntentSafe = true;

        if(isIntentSafe) {

            String format = "google.navigation:q=" + station; // setup the string to pass

            Uri uri = Uri.parse(format); // parse it into a format maps can read

            Intent launchMap = new Intent(Intent.ACTION_VIEW, uri);


            launchMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // do i need this?
          //  launchMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchMap.setPackage("com.google.android.apps.maps"); // choose the google maps app
            this.startActivity(launchMap);

        }
        else{
            // error message here
        }


    }


    private static class HttpHandler extends AsyncTask<Void, Integer, String> {

        private String data ="";
        private ArrayList<Integer> theLocation = new ArrayList<>();
        private ArrayList<String> locationStringList = new ArrayList<>();

        private int count = 0;
        private double lat = 0;
        private double lng = 0;
        private int firstChoiceNumb, secondChoiceNumb, thirdChoiceNumb;
        private int firstIndex, secondIndex, thirdIndex;
        private String closestE85Address,secondClosestStation, thirdClosestStation;
        private String suburb;
        private WeakReference<MainActivity> activityWeakReference;


        // only retain a weak reference to the activity
        HttpHandler(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected  void onPreExecute()
        {
            MainActivity activity = activityWeakReference.get();

            lat = activity.getUserLocationLatitude();
            lng = activity.getUserLocationLongitude();

        }

        @Override
        protected String doInBackground(Void... voids) {

            String locationString = MainActivity.getLocationsToSend();



                try {

                   // publishProgress(count);

                    URL testingParsedDestination = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + lat + "," + lng + "&destinations=" + locationString + "&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");

                    URL hardCodedTest = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + lat + "," + lng + "&destinations=-33.901877,151.037178&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");

                    HttpURLConnection httpURLConnection = (HttpURLConnection) testingParsedDestination.openConnection();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";

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

                    int[][] stationsAndTheirDuration = new int[elements.length()][2];

                    for (int i = 0; i < elements.length(); ++i) {

                        JSONObject objects = elements.getJSONObject(i);

                        JSONObject durationObject = objects.getJSONObject("duration_in_traffic");

                        theLocation.add(durationObject.getInt("value"));
                        locationStringList.add(destAddresses.getString(i));

                        // Append 2D array
                        stationsAndTheirDuration[i][0] = theLocation.get(i);
                        stationsAndTheirDuration[i][1] = i;

                    }


                    ArrayList<Integer> theLocationContainer = new ArrayList<>(theLocation);

                    for (String d : locationStringList) {
                        Log.d("checkDestinationStrings", d);
                    }

                    Collections.sort(theLocationContainer);


                    firstChoiceNumb = theLocationContainer.get(0);
                    secondChoiceNumb = theLocationContainer.get(1);
                    thirdChoiceNumb = theLocationContainer.get(2);

                    firstIndex = theLocation.indexOf(firstChoiceNumb);
                    secondIndex = theLocation.indexOf(secondChoiceNumb);
                    thirdIndex = theLocation.indexOf(thirdChoiceNumb);

                    closestE85Address = (destAddresses.getString(firstIndex));
                    secondClosestStation = (destAddresses.getString(secondIndex));
                    thirdClosestStation = (destAddresses.getString(thirdIndex));



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            return closestE85Address;
        }


        @Override
        protected void onPostExecute(String output) {

            MainActivity activity = activityWeakReference.get();
            activity.progressBar.setVisibility(View.GONE);

            if (output != null && stopMapsLaunching) {
                super.onPostExecute(output);

                // modify the activity's UI
                activity.firstStation.setVisibility(View.VISIBLE);
                activity.firstStation.setText(closestE85Address.replace(", Australia", ""));
                activity.secondStation.setVisibility(View.VISIBLE);
                activity.secondStation.setText(secondClosestStation.replace(", Australia", ""));
                activity.thirdStation.setVisibility(View.VISIBLE);
                activity.thirdStation.setText(thirdClosestStation.replace(", Australia", ""));


            }
            else{

                // take only the suburb from the output
                int i = closestE85Address.indexOf(',');
                String rest = closestE85Address.substring(i+2);
                int d = rest.indexOf(" ");
                suburb = rest.substring(0, d);
                Log.d("stringSplitter", suburb); // check if it works



                activity.launchMaps(closestE85Address);


            }


        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//           // txt.setText("Running..."+ values[0]);
//            MainActivity activity = activityWeakReference.get();
//            activity.progressBar.setProgress(values[0]);
//
//        }

    }

}
