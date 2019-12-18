package com.example.rocky.finde85now_001;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.Iterator;


public class HttpHandler extends AsyncTask<Void,Void,Void> {

   private String data ="";
   private String dataParsed = "";
   private int index = 0;
   private static ArrayList<Integer> theLocation = new ArrayList<>();
   private String goldenAddress = " ";

   private double lat = MainActivity.getUserLocationLatitude();
   private double lng = MainActivity.getUserLocationLongitude();

   private WeakReference<Context> contextRef;

   public HttpHandler(Context context) {
        contextRef = new WeakReference<>(context);
    }


    @Override
    protected Void doInBackground(Void... voids){

        String locationString = MainActivity.getLocationsToSend();

        try {

            URL testingParsedDestination = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + lat + "," + lng + "&destinations=" + locationString + "&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");

            URL hardCodedTest = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + lat + "," + lng +"&destinations=-33.901877,151.037178&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");

            HttpURLConnection httpURLConnection = (HttpURLConnection) testingParsedDestination.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }

                //Parse the data in a readable manner

                JSONObject JO = new JSONObject(data);
                JSONArray rowsArray = JO.getJSONArray("rows");
                JSONArray destAddresses = JO.getJSONArray("destination_addresses");
                JSONObject row0 = rowsArray.getJSONObject(0);
                JSONArray elements = row0.getJSONArray("elements");

             int closestLocation = 20000; // HARDCODED NUMBER

           for (int i = 0; i < elements.length (); ++i) {

               JSONObject objects = elements.getJSONObject(i);

               JSONObject durationObject = objects.getJSONObject("duration_in_traffic");

               theLocation.add(durationObject.getInt("value"));

               if(closestLocation > theLocation.get(i)) {
                   closestLocation = theLocation.get(i);
                   index = i;
               }

           }

            if(theLocation.get(0) == closestLocation) {
                index = 0;
            }

                // OLD HARDCODED IMPLEMENTATION, KEEPING INCASE ABOVE DOESNT WORK CORRECTLY

//                JSONObject element1 = elements.getJSONObject(1);
//                JSONObject element2 = elements.getJSONObject(2);
//                JSONObject element3 = elements.getJSONObject(3);
//
//
//            JSONObject durationObject0 = element0.getJSONObject("duration_in_traffic");
//            JSONObject durationObject1 = element1.getJSONObject("duration_in_traffic");
//            JSONObject durationObject2 = element2.getJSONObject("duration_in_traffic");
//            JSONObject durationObject3 = element3.getJSONObject("duration_in_traffic");
//
//
//            location[0] = durationObject0.getInt("value");
//            location[1] = durationObject1.getInt("value");
//            location[2] = durationObject2.getInt("value");
//            location[3] = durationObject3.getInt("value");


            // compare elements then take the element which wins and use the number to get the address

          // int closestLocation = location[0];

//          for(int i = 1; i <= 4; i++){
//
//              if(closestLocation > location[i]) {
//                  closestLocation = location[i];
//                  index = i;
//              }
//         }
//              if(location[0] == closestLocation) {
//                index = 0;
//              }

            goldenAddress = (destAddresses.getString(index));
             String singleParsed = "destination address: " + goldenAddress;

                dataParsed = dataParsed + singleParsed + "\n";

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        } catch (JSONException e) {
        e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Context context = contextRef.get();
        if (context != null) {
            // do whatever you'd like with context

            String format = "google.navigation:q=" + goldenAddress; // setup the string to pass

            Uri uri = Uri.parse(format); // parse it into a format maps can read

            Intent launchMap = new Intent(Intent.ACTION_VIEW, uri);

            launchMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // do i need this?
            launchMap.setPackage("com.google.android.apps.maps"); // choose the google maps app
            context.startActivity(launchMap);


        }
    }
}



