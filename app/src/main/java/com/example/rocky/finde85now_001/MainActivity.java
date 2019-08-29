package com.example.rocky.finde85now_001;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.net.URL;
import java.util.ArrayList;

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
    public static TextView data;

    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    float[] straightLineDistanceInMeters = new float[1];

    //Arrays to hold station lists
    double dist[] = new double[10];
    ArrayList<Double> possibleDest = new ArrayList<>();

    Location phone = new Location("phone");
    Location locationA = new Location("DeeWhy");
    Location m = new Location("test");
    LatLng currentLocation = new LatLng(0, 0); // testing latlng object


    private double homeLat = 0.0;
    private double homeLng = 0.0;

    final static double homeLat1 = -33.926360;
    final static double homeLng1 = 151.121270;

    // e85 in Sydney

    final static double deeWhyE85Lat = -33.755790;
    final static double deeWhyE85Lng = 151.282715;

    final static double vineyardE85Lat = -33.649917;
    final static double vineyardE85Lng = 150.862685;

    final static double eastBlaxlandE85Lat = -33.746039;
    final static double eastBlaxlandE85Lng = 150.622454;

    final static double rozelleE85Lat = -33.861967;
    final static double rozelleE85Lng = 151.167653;

    final static double waterlooE85Lat = -33.901910;
    final static double waterlooE85Lng = 151.208229;

    final static double yagonnaE85Lat = -33.901877;
    final static double yagonnaE85Lng = 151.037178;

    final static double yagonna1E85Lat = -33.899258;
    final static double yagoona1E85Lng = 151.036924;

    final static double prairiewoodE85Lat = -33.872234;
    final static double prairiewoodE85Lng = 150.900077;

    final static double mintoE85Lat = -34.030073;
    final static double mintoE85Lng = 150.831892;

    final static double rydalmereE85Lat = -33.810202;
    final static double rydalemereE85Lng = 151.032491;


    public double getUserLocationLatitude() {

        return userLocationLatitude;
    }

    public double getUserLocationLongitude() {
        return userLocationLongitude;
    }

    private static double userLocationLongitude;
    private static double userLocationLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        click = findViewById(R.id.button);
        data = findViewById(R.id.fetchedData);


        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpHandler process = new HttpHandler();
                process.execute();
            }
        });


        // Construct a FusedLocationProviderClient.
        //mFusedLocationProviderClient = getFusedLocationProviderClient(this);


        // TEST PERMISSION IS GRANTED
        String checkifPermissionIsGranted;
        if (mLocationPermissionGranted) {

            checkifPermissionIsGranted = "locationAllowed";
        } else {
            checkifPermissionIsGranted = "locationdenied";
        }


        //TextView textView = findViewById(R.id.result);

        //textView.setText("lastKnownLocation = " + userLocationLongitute + " " + userLocationLatitude);


        // SEND THE STATION TO GOOGLE MAPS TO LAUNCH DIRECTIONS TO IT


//        String format = "google.navigation:q=" + lat + "," + lng + "&mode=d"; // setup the string to pass
//
//        Uri uri = Uri.parse(format); // parse it into a format maps can read

//        Intent launchMap = new Intent(Intent.ACTION_VIEW, uri);
//        launchMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // do i need this?
//        launchMap.setPackage("com.google.android.apps.maps"); // choose the google maps app
//        startActivity(launchMap);

//        android.os.Process.killProcess(android.os.Process.myPid()); // kill the process running this activity

        startLocationUpdates();
        getLocationPermission();
        getLastLocation();
        //getDeviceLocation();
        // getDistanceBetween();

        for (double d : possibleDest) {
            String numberAsString = Double.toString(d);
            Log.d("DESTINATIONS", numberAsString); // testing the straight line distances in meters for all stations in syd
        }
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
            return;
        }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here

                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void getLastLocation() {

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
            // CHECK FOR PERMISSION
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                            userLocationLongitude = location.getLongitude();
                            userLocationLatitude = location.getLatitude();

                            data.setText("Longitute: " + userLocationLongitude + "\nLatitude: " + userLocationLatitude);

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


    // THIS is a version of getting current location that didnt update correctly
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location!=null) {

                    userLocationLongitude = location.getLongitude();
                    userLocationLatitude = location.getLatitude();

                    data.setText("Longitute: " + userLocationLongitude + "\nLatitude: " + userLocationLatitude);

                    // TODO: double setting user location not needed - refactor all uses of userLocationLongitude -> Device

                    homeLng = userLocationLongitude;
                    homeLat = userLocationLatitude;

                }
                else{data.setText("location is null");}
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



 // THIS DOES NOT WORK FIND A WAY TO MAKE THIS WORK
//    private void getDeviceLocation1() {
//
//        try {
//            if (mLocationPermissionGranted) {
//                mFusedLocationProviderClient.getLastLocation()
//                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                            @Override
//                            public void onSuccess(Location location) {
//                                // Got last known location. In some rare situations this can be null.
//                                if (location != null) {
//                                    // Logic to handle location object
//
//
//
//                                    userLocationLongitude = location.getLongitude();
//                                    userLocationLatitude = location.getLatitude();
//
//                                    homeLat = userLocationLatitude;
//                                    homeLng = userLocationLongitude;
//
//                                    data.setText("Longitute: " + location.getLongitude() + "\nLatitude: " + homeLng);
//
//
//
//
//                                    onLocationChanged(location);
//
//                                }
//                                else{data.setText("location is null");}
//                            }
//
//                        });
//
//            }
//        } catch (SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        homeLng = location.getLongitude();
        homeLat = location.getLatitude();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

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

                    // permission was granted, yay! Do the things
                 } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void getDistanceBetween() {

        // How to assign the correct stations to the distances??


        phone.setLongitude(userLocationLongitude);
        phone.setLatitude(userLocationLatitude);
//
       locationA.setLatitude(deeWhyE85Lat);
        locationA.setLongitude(deeWhyE85Lng);
//
//        dist[0] = test.distanceTo(locationA);
    String number = Double.toString(homeLat);
       Log.d("distanceBetweenMethod", number);

        Location.distanceBetween(rydalmereE85Lat, rydalemereE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
        dist[0] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(deeWhyE85Lat, deeWhyE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[0] = straightLineDistanceInMeters[0];
        Location.distanceBetween(vineyardE85Lat, vineyardE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
        dist[1] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(eastBlaxlandE85Lat, eastBlaxlandE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[3] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(rozelleE85Lat, rozelleE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[4] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(waterlooE85Lat, waterlooE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[5] = straightLineDistanceInMeters[0];
        Location.distanceBetween(yagonnaE85Lat, yagonnaE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
        dist[2] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(yagonna1E85Lat, yagoona1E85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[7] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(prairiewoodE85Lat, prairiewoodE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[8] = straightLineDistanceInMeters[0];
//        Location.distanceBetween(mintoE85Lat, mintoE85Lng, userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);
//        dist[9] = straightLineDistanceInMeters[0];

        //checkForStationsWithinRange and store the ones within 95km straight line distance

        for(int i = 0; i<dist.length; i++ ){
            if(dist[i] < 40000){
                possibleDest.add(dist[i]);

            }

        }


    }


}

/**
 COMPLETED FUNCTIONALITY
 -----------------------------------------------------------------------------------------------------
 - Taking current GPS position of phone and calculating the distance to a fixed location in seconds
 - get a list of stations within 200km and plug them into the URL request
 - get straight line distances from phone to all Syd stations then keep all below 45km

 ITEMS TO COMPLETE
 ------------------------------------------------------------------------------------------------------
 - Take the Json output of multiple possible destinations and check which is the closest in seconds
 - Store winning stations Coord pos and send the maps request with those coords over to the offical app
 - Compliance for app store listing to be complete
 - Ask for permission before first launch


 */

