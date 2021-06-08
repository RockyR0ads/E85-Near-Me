package com.example.rocky.finde85now_001;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.net.URL;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;

import static java.io.FileDescriptor.in;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    /*
    ISSUES TO FIX
        - first time use crashes the app as we dont yet have permission to access location (possible fix: put app in pause state while user accepts location permission)

     */
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted,findClosestStationPressed = false;


    Button findClosestStation,stationsNearMe,firstStation,secondStation,thirdStation,firstStationDetails,secondStationDetails, thirdStationDetails,fourthStation, fifthStation, moreStations,fourthStationDetails,fifthStationDetails,navigate,fcsDetails;
    TextView data, error, errorCheck, stateWatch;

    private double userLocationLongitude, userLocationLatitude;
    Context context;
    private ProgressBar progressBar;
    SupportMapFragment mapFragment;
    LatLng closestStation;
    private static Boolean stopMapsLaunching = false;


    final static double homeLat = -33.926360;
    final static double homeLng = 151.121270;

    StationHandler stationHandler;
    Station station;
    Resources res;
    DataParser dataParser;

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

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getView().setVisibility(View.INVISIBLE);

        findClosestStation = findViewById(R.id.button);
        data = findViewById(R.id.fetchedData);
        error = findViewById(R.id.Error);
        stationsNearMe = findViewById(R.id.stationsNearMe);
        firstStation = findViewById(R.id.firstStation);
        secondStation = findViewById(R.id.secondStation);
        thirdStation = findViewById(R.id.thirdStation);
        fourthStation = findViewById(R.id.fourthStation);
        fifthStation = findViewById(R.id.fifthStation);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        progressBar = findViewById(R.id.progressBar);
        errorCheck = findViewById(R.id.errorBoi);
        stateWatch = findViewById(R.id.state);
        firstStationDetails = findViewById(R.id.firstStationDetails);
        secondStationDetails = findViewById(R.id.secondStationDetails);
        thirdStationDetails = findViewById(R.id.thirdStationDetails);
        fourthStationDetails = findViewById(R.id.fourthStationDetails);
        fifthStationDetails = findViewById(R.id.fifthStationDetails);
        moreStations = findViewById(R.id.moreStationsNearMe);
        navigate = findViewById(R.id.navigate);
        fcsDetails = findViewById(R.id.closestStationDetails);

        stationHandler = new StationHandler();
        stationHandler.initialiseStations();
        station = new Station();
        res = getResources();
        dataParser = new DataParser();

        final Drawable red = res.getDrawable(R.drawable.btn_rounded_red);
        final Drawable green = res.getDrawable(R.drawable.btn_rounded_green);

        stateWatch.setText(this.getLifecycle().getCurrentState().toString());

        getDeviceLocation();

        this.stateWatch.setText(this.getLifecycle().getCurrentState().toString());

        // FIND CLOSEST STATION
        findClosestStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideSNM();
                stopMapsLaunching = false;
                findClosestStationPressed = true;
                HttpHandler asyncTask = new HttpHandler(MainActivity.this);
                progressBar.setVisibility(View.VISIBLE);
                animateProgressBar();
                asyncTask.execute();

            }
        });

        // FIND STATIONS NEAR ME
        stationsNearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(findClosestStationPressed){
                    mapFragment.getView().setVisibility(View.GONE);
                    navigate.setVisibility(View.GONE);
                    fcsDetails.setVisibility(View.GONE);
                }
                stopMapsLaunching = true;
                HttpHandler asyncTask = new HttpHandler(MainActivity.this);
                progressBar.setVisibility(View.VISIBLE);
                animateProgressBar();
                asyncTask.execute();


            }
        });

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStations().get(0));
            }
        });
        firstStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStations().get(0));
            }
        });
        secondStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStations().get(1));
            }
        });
        thirdStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStations().get(2));
            }
        });
        fourthStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStations().get(3));
            }
        });
        fifthStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps(station.getClosestStations().get(4));
            }
        });
        firstStationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               buildDetailsDialog(stationHandler.getClosestStations().get(0));
            }
        });
        secondStationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDetailsDialog(stationHandler.getClosestStations().get(1));
            }
        });
        thirdStationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDetailsDialog(stationHandler.getClosestStations().get(2));
            }
        });
        fourthStationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDetailsDialog(stationHandler.getClosestStations().get(3));
            }
        });
        fifthStationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDetailsDialog(stationHandler.getClosestStations().get(4));
            }
        });

        moreStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moreStations.setVisibility(View.INVISIBLE);
                fourthStation.setVisibility(View.VISIBLE);
                fifthStation.setVisibility(View.VISIBLE);
                fourthStationDetails.setVisibility(View.VISIBLE);
                fifthStationDetails.setVisibility(View.VISIBLE);

                if(stationHandler.getStationByAddress(stationHandler.getClosestStations().get(3).getFullAddress()).isTheStationOpen()){
                    fourthStation.setBackground(green);
                }else{
                    fourthStation.setBackground(red);
                }

                if(stationHandler.getStationByAddress(stationHandler.getClosestStations().get(4).getFullAddress()).isTheStationOpen()){
                    fifthStation.setBackground(green);
                }else{
                    fifthStation.setBackground(red);
                }

                fourthStation.setText(stationHandler.snmStringConstruct(stationHandler.getClosestStations().get(3).getFullAddress()));
                fifthStation.setText(stationHandler.snmStringConstruct(stationHandler.getClosestStations().get(4).getFullAddress()));
            }
        });

        fcsDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDetailsDialog(stationHandler.getClosestStations().get(0));
            }
        });

    }

    private void hideSNM(){
        firstStation.setVisibility(View.GONE);
        firstStationDetails.setVisibility(View.GONE);
        secondStation.setVisibility(View.GONE);
        secondStationDetails.setVisibility(View.GONE);
        thirdStation.setVisibility(View.GONE);
        thirdStationDetails.setVisibility(View.GONE);
        moreStations.setVisibility(View.GONE);

        if(fourthStation.getVisibility() == View.VISIBLE){
            fourthStation.setVisibility(View.GONE);
            fifthStation.setVisibility(View.GONE);
            fourthStationDetails.setVisibility(View.GONE);
            fifthStationDetails.setVisibility(View.GONE);

        }
    }

    private void prepareSNMUI(){
        final Drawable red = res.getDrawable(R.drawable.btn_rounded_red);
        final Drawable green = res.getDrawable(R.drawable.btn_rounded_green);

        // modify the activity's UI
        if(stationHandler.getStationByAddress(stationHandler.getClosestStations().get(0).getFullAddress()).isTheStationOpen()){
            firstStation.setBackground(green);
        }else{firstStation.setBackground(red);}

        if(stationHandler.getStationByAddress(stationHandler.getClosestStations().get(1).getFullAddress()).isTheStationOpen()){
            secondStation.setBackground(green);
        }else{secondStation.setBackground(red);}

        if(stationHandler.getStationByAddress(stationHandler.getClosestStations().get(2).getFullAddress()).isTheStationOpen()){
            thirdStation.setBackground(green);
        }else{thirdStation.setBackground(red);}

        firstStation.setVisibility(View.VISIBLE);
        firstStationDetails.setVisibility(View.VISIBLE);
        firstStation.setText(stationHandler.snmStringConstruct(stationHandler.getClosestStations().get(0).getFullAddress()));

        secondStation.setVisibility(View.VISIBLE);
        secondStation.setText(stationHandler.snmStringConstruct(stationHandler.getClosestStations().get(1).getFullAddress()));
        secondStationDetails.setVisibility(View.VISIBLE);

        thirdStation.setVisibility(View.VISIBLE);
        thirdStation.setText(stationHandler.snmStringConstruct(stationHandler.getClosestStations().get(2).getFullAddress()));
        thirdStationDetails.setVisibility(View.VISIBLE);

        if(stationHandler.getClosestStations().size() > 4){
            moreStations.setVisibility(View.VISIBLE);
        }

        stateWatch.setText("state:" + getLifecycle().getCurrentState().toString());
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

                                    //userLocationLatitude = location.getLatitude();
                                   //userLocationLongitude = location.getLongitude();

                                    // Moore Creek test
                                   // userLocationLatitude = -34.790970;
                                  //  userLocationLongitude = 147.025000;

                                    // Bourke (not within 500km of any station)test
//                                    userLocationLatitude = -30.0914494;
//                                    userLocationLongitude = 145.9429902;

                                    // north shore test
                                   // userLocationLatitude = -33.763691;
                                   // userLocationLongitude = 151.21759;

                                    // north NSW Test
                                    userLocationLatitude = -32.334603;
                                     userLocationLongitude = 151.291866;


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
                error.setText(" This \n application \n requires \n location \n permissions \n to run");
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
        this.finishAffinity();
    }

    private void buildDetailsDialog(Station s){

        TextView name,open,closed,address,kmsView,time;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View mView = inflater.inflate(R.layout.details_dialog, null);
        final Station station = s;
        String kms = " ";
        String timeInMinutes = " ";

        name = mView.findViewById(R.id.stationName);
        open = mView.findViewById(R.id.openTime);
        closed = mView.findViewById(R.id.closeTime);
        address = mView.findViewById(R.id.address);
        kmsView = mView.findViewById(R.id.kms);
        time = mView.findViewById(R.id.timeToStation);



        for(int i = 0; i < stationHandler.addressesReturned.size(); i ++){
            if(s.getFullAddress().equals(stationHandler.addressesReturned.get(i))){
              kms = stationHandler.distanceToStation.get(i);
               timeInMinutes = stationHandler.timeToStation.get(i);
            }
        }

        name.setText("Station: " + s.getCompany() + " " + s.getSuburb());
        open.setText("Open Time: " + s.changeTimeFormat(s.getOpeningTime()));
        closed.setText("Close Time: " + s.changeTimeFormat(s.getClosingTime()));
        address.setText("Address: " + s.getFullAddress());
        kmsView.setText("Distance to Station: " + kms);
        time.setText("Time to Station: " + timeInMinutes);

        // cover the case of 24/7 stations
        if(s.getOpeningTime()==0 && s.getClosingTime()==2400){
            open.setText("Opening Time: " + "24 Hours");
            closed.setText("Closing Time: " + "24 Hours");
        }

        builder.setView(mView);
        builder.setMessage("Station Details");
        builder.setIcon(android.R.drawable.btn_star)

                .setPositiveButton("Navigate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do what when you click add
                    launchMaps(station.getFullAddress());

                    }
                })
                .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();

                    }
                });

        // Create the AlertDialog object and return it
        AlertDialog builder1 = builder.create();
        builder1.show();

    }



    void showMapUI(){

        mapFragment.getMapAsync(MainActivity.this);
        mapFragment.getView().setVisibility(View.VISIBLE);
        navigate.setVisibility(View.VISIBLE);
        fcsDetails.setVisibility(View.VISIBLE);
    }

    void buildDialog(){


        Station s = stationHandler.getStationByAddress(station.getClosestStations().get(0));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
       // builder.setTitle(s.getCompany() + " " + s.getSuburb() + " is CLOSED");
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

    public void buildFailureDialog(){

         AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("There are no stations within 500Km of your position");
        builder.setIcon(android.R.drawable.ic_dialog_alert)

                .setPositiveButton("exit app", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        System.exit(0);

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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        GoogleMap mMap = googleMap;
        LatLng aus = new LatLng(-25.3455545, 131.0369615); // literally Uluru lmao

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aus,3.5f));

        if(closestStation!=null){
            mMap.addMarker(new MarkerOptions()
                    .position(closestStation)
                    .title("Marker in Sydney"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(closestStation,15));
            mMap.setTrafficEnabled(true);

        }


    }

    private static class HttpHandler extends AsyncTask<Void, Integer, String> {
        DataParser dp = new DataParser();
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

            String theTest = " ";
            try {

                    URL testingParsedDestination = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + activity.getUserLocationLatitude() + "," + activity.getUserLocationLongitude() + "&destinations=" + locationString + "&departure_time=now&key=AIzaSyAdGvjhZOCghKo-Y7Sl-_A4dsT1L9W6QcI");
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

                theTest = dp.parseAPIdata(JO, activity.context);
//
//                String checkRequest = JO.getString("status");
//
//                Log.d("checkInvalidLog", "Checking INVALID REQUEST");
//
//                if (checkRequest.equals("INVALID_REQUEST")) {
//                    Log.d("checkInvalidLog1", "INVALID REQUEST");
//                    return "FAIL";
//                }
//
//                JSONArray rowsArray = JO.getJSONArray("rows");
//                JSONArray destAddresses = JO.getJSONArray("destination_addresses");
//                JSONObject row0 = (JSONObject) rowsArray.get(0);
//                JSONArray elements = row0.getJSONArray("elements");
//
//                String test = rowsArray.toString();
//
//                for (int i = 0; i < elements.length(); ++i) {
//
//                    JSONObject objects = elements.getJSONObject(i);
//
//                    activity.stationHandler.timeToArriveInTraffic.add(objects.getJSONObject("duration_in_traffic").getInt("value"));
//                    activity.stationHandler.distanceToStation.add(objects.getJSONObject("distance").getString("text"));
//                    activity.stationHandler.timeToStation.add(objects.getJSONObject("duration_in_traffic").getString("text"));
//                    activity.stationHandler.addressesReturned.add(destAddresses.getString(i));
//
//
//                }
//
//
//                ArrayList<Integer> minutesToDestination = new ArrayList<>(activity.stationHandler.timeToArriveInTraffic);
//
//                for (String d : activity.stationHandler.addressesReturned) {
//                    Log.d("checkDestinationStrings", d);
//                }
//
//                Collections.sort(minutesToDestination);
//
//                for(int i= 0; i < minutesToDestination.size(); i++) {
//                     activity.station.setClosestStationAddress((destAddresses.getString(activity.stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(i)))));
//                     activity.stationHandler.setClosestStations((destAddresses.getString(activity.stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(i)))));
//                }
//
//
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (JSONException e) {
                e.printStackTrace();
           }
            //catch (Exception e) {
////                e.printStackTrace();
////            }


          //  return activity.station.getClosestStations().get(0);
          //  return activity.stationHandler.getClosestStations().get(0).getFullAddress();
            return theTest;
        }


        @Override
        protected void onPostExecute(@NonNull String output) {

            MainActivity activity = activityWeakReference.get();
            activity.progressBar.setVisibility(View.GONE);
            Drawable red = activity.res.getDrawable(R.drawable.btn_rounded_red);
            Drawable green = activity.res.getDrawable(R.drawable.btn_rounded_green);


            activity.stationHandler = dp.stationHandler;

           // dp.displayResults(output,activity.context);

            if ((!output.equals("FAIL")) && stopMapsLaunching) { // user wants to see the 3 closest stations
                super.onPostExecute(output);
                activity.prepareSNMUI();

            } else if(!output.equals("FAIL")) {
                    if (activity.stationHandler.getStationByAddress(activity.stationHandler.getClosestStations().get(0).getFullAddress()).isTheStationOpen()) { // station is open send the user to maps
                       // activity.launchMaps(activity.station.getClosestStations().get(0));
                        activity.closestStation = new LatLng(activity.stationHandler.getClosestStations().get(0).getLatitude(),activity.stationHandler.getClosestStations().get(0).getLongitude());
                      //  activity.finish();
                        activity.showMapUI();


                    } else { // station is closed
                            activity.buildDialog();
                            activity.errorCheck.setText("Station is not open Go EAT ASS");
                            activity.stateWatch.setText("state:" + activity.getLifecycle().getCurrentState().toString());

                    }

            }
            else{

                activity.buildFailureDialog();
            }
        }
    }
}
