package com.example.rocky.finde85now_001;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;


public class HttpHandler extends AsyncTask<Void,Void,Void> {

   private String data ="";
   private String dataParsed = "";
   private String singleParsed = "";
   private String urlAdd = " ";

    MainActivity MA = new MainActivity();
    private double lat = MA.getUserLocationLatitude();
    private double lng = MA.getUserLocationLongitude();

   // String Slat = String.valueOf(lat);


    @Override
    protected Void doInBackground(Void... voids){

        try {

            URL url0 = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + lat + "," + lng +"&destinations=Rydalmere,NSW&departure_time=now&key=AIzaSyAMxY0HN35WCTUM6SGl1ngqsx6zC8t_5Lk");



            HttpURLConnection httpURLConnection = (HttpURLConnection) url0.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }


            //Parse the data in a readable manner

            //JSONArray JA = new JSONArray(data); // get the Array

            JSONObject JO = new JSONObject(data);


            JSONArray rowsArray = JO.getJSONArray("rows");

            JSONObject row0 = rowsArray.getJSONObject(0);

            JSONArray elements = row0.getJSONArray("elements");

            JSONObject element0 = elements.getJSONObject(0);

            JSONObject durationObject = element0.getJSONObject("duration");
            String durationInSeconds = durationObject.getString("value");


            singleParsed = "seconds to destination: " + durationInSeconds;

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

       MainActivity.data.setText(this.singleParsed);
    }
}



