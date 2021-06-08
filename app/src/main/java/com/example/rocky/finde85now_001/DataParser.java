package com.example.rocky.finde85now_001;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class DataParser {

    StationHandler stationHandler = new StationHandler();
    MainActivity mainActivity;

    Station station = new Station();
    String locationString = StationHandler.getLocationsToSend();


    public String parseAPIdata(JSONObject data, Context context) {
        
        stationHandler.initialiseStations();


        try {
            String checkRequest = data.getString("status");

            Log.d("checkInvalidLog", "Checking INVALID REQUEST");

            if (checkRequest.equals("INVALID_REQUEST")) {
                Log.d("checkInvalidLog1", "INVALID REQUEST");
                return "FAIL";
            }

            JSONArray rowsArray = data.getJSONArray("rows");
            JSONArray destAddresses = data.getJSONArray("destination_addresses");
            JSONObject row0 = (JSONObject) rowsArray.get(0);
            JSONArray elements = row0.getJSONArray("elements");

            String test = rowsArray.toString();

            for (int i = 0; i < elements.length(); ++i) {

                JSONObject objects = elements.getJSONObject(i);

                stationHandler.timeToArriveInTraffic.add(objects.getJSONObject("duration_in_traffic").getInt("value"));
                stationHandler.distanceToStation.add(objects.getJSONObject("distance").getString("text"));
                stationHandler.timeToStation.add(objects.getJSONObject("duration_in_traffic").getString("text"));
                stationHandler.addressesReturned.add(destAddresses.getString(i));


            }


            ArrayList<Integer> minutesToDestination = new ArrayList<>(stationHandler.timeToArriveInTraffic);

            for (String d : stationHandler.addressesReturned) {
                Log.d("checkDestinationStrings", d);
            }

            Collections.sort(minutesToDestination);

            for (int i = 0; i < minutesToDestination.size(); i++) {
                station.setClosestStationAddress((destAddresses.getString(stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(i)))));
                stationHandler.setClosestStations((destAddresses.getString(stationHandler.timeToArriveInTraffic.indexOf(minutesToDestination.get(i)))));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stationHandler.getClosestStations().get(0).getFullAddress();
    }

    public void displayResults(String output,Context context){


        boolean stopMapsLaunching = true;
        if ((!output.equals("FAIL")) && stopMapsLaunching) { // user wants to see the 3 closest stations



        } else if(!output.equals("FAIL")) {
            if (stationHandler.getStationByAddress(stationHandler.getClosestStations().get(0).getFullAddress()).isTheStationOpen()) { // station is open send the user to maps
                // activity.launchMaps(activity.station.getClosestStations().get(0));
                mainActivity.closestStation = new LatLng(stationHandler.getClosestStations().get(0).getLatitude(),stationHandler.getClosestStations().get(0).getLongitude());
                //  activity.finish();
                mainActivity.showMapUI();


            } else { // station is closed
                mainActivity.buildDialog();
                mainActivity.errorCheck.setText("Station is not open Go EAT ASS");
                mainActivity.stateWatch.setText("state:" + mainActivity.getLifecycle().getCurrentState().toString());

            }

        }
        else{

            mainActivity.buildFailureDialog();
        }
    }
}
