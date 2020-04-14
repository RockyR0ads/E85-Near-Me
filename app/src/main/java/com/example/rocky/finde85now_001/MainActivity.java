package com.example.rocky.finde85now_001;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
    TextView errorCheck;
    TextView stateWatch;

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

    private ArrayList<String> shortlistedDestinations = new ArrayList<>();
    private ArrayList<Station> shortlistedDestinations1 = new ArrayList<>();
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


        HH = new HttpHandler(this);

        stateWatch.setText(this.getLifecycle().getCurrentState().toString());

        stations.add(new Station (-33.649917,150.862685,530,2130, "Vineyard", false));
        stations.add(new Station (-33.810202,151.032491,600,2200, "Rydalmere",false));
        stations.add(new Station (-33.861967,151.167653,0,2400, "Rozelle",true));
        stations.add(new Station (-33.901910,151.208229,0,2400, "Waterloo",true));
        stations.add(new Station (-33.901877,151.037178,500,2400, "Yagoona",false));
        stations.add(new Station (-33.755790,151.282715,500,2400, "Dee Why",false));
        stations.add(new Station (-33.746039,150.622454,600,2100, "Blaxland",false));
        stations.add(new Station (-33.899258,151.036924,600,2200, "Yagoona1",false));
        stations.add(new Station (-33.872234,150.900077,600,2200, "Prairiewood",false));
        stations.add(new Station (-34.030073,150.831892,0,2400, "Minto",true));
        stations.add(new Station (-33.680160,151.225010,700,2200, "Terrey Hills",false));

        //Testing if my class works
        String s = String.valueOf(stations.get(0).getClosingTime());
        stations.get(0).setFullAddress("1540 Windsor Road, Vineyard, NSW 2765, Australia");
        Log.d("VineyardCloseTime", s); // result = 2130 (SUCCESS)


            // store all SYDNEY United stations in array

            storedStations[0] = -33.649917; // vineyard
            storedStations[1] = 150.862685;

            storedStations[2] = -33.901877; // yagoona45
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




        getDeviceLocation();

        this.stateWatch.setText(this.getLifecycle().getCurrentState().toString());

        // FIND CLOSEST STATION
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopMapsLaunching = false;
                HH.execute();
               // finish();

            }
        });

        // FIND STATIONS NEAR ME
        stationsNearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopMapsLaunching = true;
                progressBar.setVisibility(View.VISIBLE);
                animateProgressBar();
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

    private boolean isTheStationOpen(int opening, int closing)
    {
        String currentTime;
        int timeNow;
        boolean check = false;

        //get current user time
        currentTime = new SimpleDateFormat("kk:mm", Locale.getDefault()).format(new Date());
        Log.d("theCurrentTime", currentTime); // check if its working

        currentTime = toMins(currentTime);

        timeNow = Integer.valueOf(currentTime);

        //timeNow = 400; // testing for time outside of opening hours

        if (timeNow > opening && timeNow < closing) {

            check = true;
        }
            return check;
    }

    private void animateProgressBar(){

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

                                    userLocationLongitude = location.getLongitude();
                                    userLocationLatitude = location.getLatitude();

//                                    userLocationLongitude = 151.049502; // silverwater test
////                                  userLocationLatitude = -33.830092;
//
//                                    userLocationLongitude = 151.277790; // Dee Why
//                                    userLocationLatitude = -33.764022;

//                                    userLocationLongitude = 151.1442;  // brighton le sands
//                                    userLocationLatitude = -33.9627;

//                                    userLocationLatitude = -33.867970;
//                                    userLocationLongitude = 151.128870;  // five dock

//                                    userLocationLatitude = -33.653588;
//                                    userLocationLongitude = 150.868142;  // vineyard

                                    data.setText("Longitude: " + userLocationLongitude + "\nLatitude: " + userLocationLatitude);

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
    private void getDistanceBetweenOld() {

        for(int i = 0; i < storedStations.length; i++ ){

            Location.distanceBetween(storedStations[i], storedStations[i+1], userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);

            distance[i] = straightLineDistanceInMeters[0];

            // store sub 30km stations in a straight line
            if(straightLineDistanceInMeters[0] < 30000){

                shortlistedDestinations.add(storedStations[i]+"");
                shortlistedDestinations.add(storedStations[i+1]+"");
            }

            String distanceInStraightLine = Double.toString(distance[i]);
            Log.d("distanceInStraightLine", distanceInStraightLine);

            i+=1;
        }

    }
        // parallel method trying to get same result using station class instead of hard coded array

    private void getDistanceBetween() {

        for(int i = 0; i < stations.size(); i++ ){

            Location.distanceBetween(stations.get(i).getLatitude(), stations.get(i).getLongitude(), userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);

            distance[i] = straightLineDistanceInMeters[0];

            // store sub 30km stations in a straight line
            if(straightLineDistanceInMeters[0] < 30000){

                shortlistedDestinations1.add(stations.get(i));

            }

            String distanceInStraightLine = Double.toString(distance[i]);
            Log.d("distanceInStraightLine", distanceInStraightLine);

        }

    }

    // create the string of coordinates to be send in the HTTPS request based of the closest stations decided in getDistanceBetween

    private void stringConstructorOld(){

        int size;
        int testSize;
        int i = 0;

        StringBuilder sb = new StringBuilder();
        size = shortlistedDestinations.size();
        testSize = size-1;

        for (String d : shortlistedDestinations) {
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


    private void stringConstructor() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < shortlistedDestinations1.size(); i++) {

            if (i == shortlistedDestinations1.size() - 1) {

                sb.append(shortlistedDestinations1.get(i).getLatitude() + ",");
                sb.append(shortlistedDestinations1.get(i).getLongitude());
                break;
            }

            sb.append(shortlistedDestinations1.get(i).getLatitude() + ",");
            sb.append(shortlistedDestinations1.get(i).getLongitude() + "|");

        }
        locationsToSend = sb.toString();

        Log.d("locationsToSend", locationsToSend);

    }

    private void launchMaps(String station){

            String format = "google.navigation:q=" + station; // setup the string to pass

            Uri uri = Uri.parse(format); // parse it into a format maps can read

            Intent launchMap = new Intent(Intent.ACTION_VIEW, uri);


            launchMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // do i need this?
          //  launchMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchMap.setPackage("com.google.android.apps.maps"); // choose the google maps app
            this.startActivity(launchMap);
    }

    private String toMins(String time) {

       return time.replaceFirst(":", "");

    }


    private void buildDialog(){

        new AlertDialog.Builder(this,android.R.style.Theme_Holo_Dialog)
                .setTitle("Service Station is Closed")
                .setMessage("Do you want to proceed to the closest open station instead?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        launchMaps(HH.secondClosestStation);
                    }
                })


                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    // THIS DOESNT WORK
    public static class DialogFragmentContainer extends DialogFragment {

        private MainActivity mainActivityRef;
        private HttpHandler httpHandlerRef;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mainActivityRef = (MainActivity) getActivity();
            
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Holo_Dialog);
            builder.setTitle("Service Station is Closed");
            builder.setMessage("Do you want to proceed to the closest open station instead?");
            builder.setIcon(android.R.drawable.ic_dialog_alert)


                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                          // mainActivityRef.launchMaps(secondClosestStation);

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog

                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private int stringSplitter(String closestE85Station){

        int open = 0;
        int closed = 0;
        int stationIndex = 0;
        int commaIndex;

        String suburb;

        // take only the suburb from the output
        commaIndex = closestE85Station.indexOf(',');
        String street = closestE85Station.substring(0,commaIndex);
        String restOfWord = closestE85Station.substring(commaIndex + 2);
        int d = restOfWord.indexOf(" ");

        String nextWordCheck = restOfWord.substring(d+1,d+3); // grab the next 2 charachters after the space

        boolean hasLowerCase = !nextWordCheck.equals(nextWordCheck.toUpperCase()); // Check if the 2 letters are upper case or not

        if(hasLowerCase){ // if they are not the next word must be part of the suburb

            String endOfString = restOfWord.substring(d+1);
            int spaceIndex = endOfString.indexOf(" ");
            String suburbSecondPart = endOfString.substring(0, spaceIndex);
            suburb = restOfWord.substring(0, d) + " " + suburbSecondPart;

        } else{

            suburb = restOfWord.substring(0, d);

        }


        Log.d("stringSplitter", suburb); // check if it works

        for (int i = 0; i < stations.size(); ) {

            String stationSuburb = stations.get(i).getSuburb();

            if (suburb.equals(stationSuburb)) { // stations match! grab that stations index

//                open = stations.get(i).getOpeningTime();
//                closed = stations.get(i).getClosingTime();

                stationIndex = i;
                break;

            } else {
                i++;
            }

        }

        return stationIndex;

    }


    private static class HttpHandler extends AsyncTask<Void, Integer, String> {

        private ArrayList<Integer> theLocation = new ArrayList<>();
        private ArrayList<String> locationStringList = new ArrayList<>();

        private int open;
        private int closed;
        private int firstIndex, secondIndex, thirdIndex;
        private String closestE85Address,secondClosestStation, thirdClosestStation;
        private WeakReference<MainActivity> activityWeakReference;
        DialogFragment newFragment = new DialogFragmentContainer();

        // only retain a weak reference to the activity
        HttpHandler(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected  void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(Void... voids) {
            MainActivity activity = activityWeakReference.get();
            String locationString = MainActivity.getLocationsToSend();

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

                    for (int i = 0; i < elements.length(); ++i) {

                        JSONObject objects = elements.getJSONObject(i);

                        JSONObject durationObject = objects.getJSONObject("duration_in_traffic");

                        theLocation.add(durationObject.getInt("value"));
                        locationStringList.add(destAddresses.getString(i));

                    }


                    ArrayList<Integer> theLocationContainer = new ArrayList<>(theLocation);

                    for (String d : locationStringList) {
                        Log.d("checkDestinationStrings", d);
                    }

                    Collections.sort(theLocationContainer);

                    int firstChoiceNumb = theLocationContainer.get(0);
                    int secondChoiceNumb = theLocationContainer.get(1);
                    int thirdChoiceNumb = theLocationContainer.get(2);

                    firstIndex = theLocation.indexOf(firstChoiceNumb);
                    secondIndex = theLocation.indexOf(secondChoiceNumb);
                    thirdIndex = theLocation.indexOf(thirdChoiceNumb);

                    closestE85Address = (destAddresses.getString(firstIndex));
                    secondClosestStation = (destAddresses.getString(secondIndex));
                    thirdClosestStation = (destAddresses.getString(thirdIndex));



                }catch (IOException e) {
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

                activity.stateWatch.setText(activity.getLifecycle().getCurrentState().toString());

            } else {

//                activity.launchMaps(closestE85Address); // run without opening hour functionality
//                activity.finish();

                    int index = activity.stringSplitter(closestE85Address);

                        open = activity.stations.get(index).getOpeningTime();
                        closed = activity.stations.get(index).getClosingTime();

            if (activity.isTheStationOpen(open, closed)) { // station is open send the user to maps

                    activity.launchMaps(closestE85Address);
                    activity.finish();

            } else { // station is closed

                    //activity.buildDialog();
                    newFragment.show(activity.getSupportFragmentManager(),"stationClosed");
                    activity.errorCheck.setText("Station is not open Go EAT ASS");
                    activity.stateWatch.setText(activity.getLifecycle().getCurrentState().toString());

                }

               // activity.launchMaps(closestE85Address);

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
