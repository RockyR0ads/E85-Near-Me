package com.example.rocky.finde85now_001;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


class StationHandler {


    // Arrays to hold station lists
    private ArrayList<Station> stations = new ArrayList<>();
    private ArrayList<Station> firstChoiceDestinations = new ArrayList<>();
    private ArrayList<Station> secondChoiceDestinations = new ArrayList<>();
    private ArrayList<Station> thirdChoiceDestinations = new ArrayList<>();
    private double[] distance = new double[24];
    private static String locationsToSend = "";
    public ArrayList<String> addressesReturned = new ArrayList<>();
    public ArrayList<Integer> timeToArriveInTraffic = new ArrayList<>();
    public ArrayList<String> distanceToStation = new ArrayList<>();
    public ArrayList<String> timeToStation = new ArrayList<>();
    public ArrayList<Station> closestStations = new ArrayList<>();




    public static String getLocationsToSend() {
        return locationsToSend;
    }


    void initialiseStations() {
        //Syd
        stations.add(new Station(-33.649917, 150.862685, 530, 2130, "Vineyard", "Freedom", false));
        stations.add(new Station(-33.810202, 151.032491, 600, 2200, "Rydalmere", "United", false));
        stations.add(new Station(-33.861967, 151.167653, 0,   2400, "Rozelle","United",  true));
        stations.add(new Station(-33.901910, 151.208229, 0,   2400, "Waterloo","United", true));
        stations.add(new Station(-33.901877, 151.037178, 500, 2400, "Yagoona","United","100 Rookwood Rd, Yagoona NSW 2199, Australia", false));
        stations.add(new Station(-33.755790, 151.282715, 500, 2400, "Dee Why","United", false));
        stations.add(new Station(-33.746039, 150.622454, 600, 2100, "Blaxland","United", false));
        stations.add(new Station(-33.899258, 151.036924, 600, 2200, "Yagoona","United","45B Rookwood Rd, Yagoona NSW 2199, Australia", false));
        stations.add(new Station(-33.872234, 150.900077, 600, 2200, "Prairiewood","United", false));
        stations.add(new Station(-34.030073, 150.831892, 0,   2400, "Minto","United", true));
        stations.add(new Station(-33.680160, 151.225010, 700, 2200, "Terrey Hills","United", false));
        //Outside of Syd (NSW)
        stations.add(new Station(-30.290270, 153.120960, 0, 2400, "Coffs Central","United", true));
        stations.add(new Station(-33.230820, 151.503160, 0, 2400, "Leeton","United", true));
        stations.add(new Station(-35.181880, 149.235120, 0, 2400, "Sutton","United", true));
        stations.add(new Station(-34.868590, 147.583370, 500, 2300, "Junee","United", false));
        stations.add(new Station(-35.122604, 147.392120, 0, 2400, "Wagga Wagga","United", true));
        stations.add(new Station(-34.903915, 150.602692, 500, 2100, "Nowra","United", false));
        stations.add(new Station(-33.285255, 149.103729, 500, 2100, "Orange","United", false));
        stations.add(new Station(-33.360249, 151.369461, 500, 2100, "Ourimbah","United", false));
        stations.add(new Station(-33.234322, 151.555771, 600, 2100, "Budgewoi","United", false));
        stations.add(new Station(-32.751464, 151.585977, 0, 2400, "East Maitland","United", true));
        stations.add(new Station(-32.766464, 151.611816, 600, 2200, "Metford","United", false));
       // stations.add(new Station(-32.766464, 151.611816, 600, 2000, "Charlotte Bay","United", false)); fact check this station

    }


    public void getDistanceBetween(double userLocationLatitude, double userLocationLongitude) {
        float[] straightLineDistanceInMeters = new float[1];

        for (int i = 0; i < stations.size(); i++) {
            Location.distanceBetween(stations.get(i).getLatitude(), stations.get(i).getLongitude(), userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);

            distance[i] = straightLineDistanceInMeters[0];

            // store sub 50km stations in a straight line
            if (straightLineDistanceInMeters[0] < 50000) {
                firstChoiceDestinations.add(stations.get(i));
            }else if(straightLineDistanceInMeters[0] < 100000){
                secondChoiceDestinations.add(stations.get(i));
            }else if(straightLineDistanceInMeters[0] < 5000000){
                thirdChoiceDestinations.add(stations.get(i));
            }


            String distanceInStraightLine = Double.toString(distance[i]);
            Log.d("distanceInStraightLine", distanceInStraightLine);
        }
            // Logic to decide to ensure at least 3 stations are always found
            while(firstChoiceDestinations.size() < 3 && firstChoiceDestinations.size()!=0) {
                for (int i = 0; i < secondChoiceDestinations.size(); i++) {
                   // firstChoiceDestinations.add(secondChoiceDestinations.get(i));
                    if(firstChoiceDestinations.size() < 9) {
                        Collections.addAll(firstChoiceDestinations, secondChoiceDestinations.get(i));
                    }
                }
                if(firstChoiceDestinations.size() < 3){
                    for (int i = 0; i < thirdChoiceDestinations.size(); i++) {
                        if(firstChoiceDestinations.size() < 20) {
                            Collections.addAll(firstChoiceDestinations, thirdChoiceDestinations.get(i));
                        }
                    }
                }
            }
        if(firstChoiceDestinations.size() > 3) {
            stringConstructor();
        }
    }


    private int stringSplitter(String fullAddress) {
        int stationIndex = 0;
        String suburb = "";

        // take only the suburb from the output
        int commaIndex = fullAddress.indexOf(',');
        String street = fullAddress.substring(0, commaIndex);
        String restOfWord = fullAddress.substring(commaIndex + 2);
        int d = restOfWord.indexOf(" ");

        String nextWordCheck = restOfWord.substring(d + 1, d + 3); // grab the next 2 characters after the space
        boolean hasLowerCase = !nextWordCheck.equals(nextWordCheck.toUpperCase()); // Check if the 2 letters are upper case or not

        if (hasLowerCase) { // if they are lowercase the next word must be part of the suburb name
            String endOfString = restOfWord.substring(d + 1);
            int spaceIndex = endOfString.indexOf(" ");
            String suburbSecondPart = endOfString.substring(0, spaceIndex);
            suburb = restOfWord.substring(0, d) + " " + suburbSecondPart;
        } else {
            suburb = restOfWord.substring(0, d);
        }

        Log.d("stringSplitter", suburb); // check if it works

        for (int i = 0; i < stations.size(); i++) {
            String stationSuburb = stations.get(i).getSuburb();
            if (stations.get(i).getFullAddress() == null) {
                    if (suburb.equals(stationSuburb)) { // stations match! grab that stations index

                        stations.get(i).setFullAddress(fullAddress);
                        return i;
                    }
            }else if(fullAddress.equals(stations.get(i).getFullAddress())){
                return i;
            }
        }
        return stationIndex;
    }

    private void stringConstructor() {
    /*
        construct the string you need to plug into the maps API
     */
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < firstChoiceDestinations.size(); i++) {

            if (i == firstChoiceDestinations.size() - 1) {
                sb.append(firstChoiceDestinations.get(i).getLatitude()).append(",");
                sb.append(firstChoiceDestinations.get(i).getLongitude());
                break;
            }
            sb.append(firstChoiceDestinations.get(i).getLatitude()).append(",");
            sb.append(firstChoiceDestinations.get(i).getLongitude()).append("|");

        }
        locationsToSend = sb.toString();
        Log.d("locationsToSend", locationsToSend);
    }

    public String findOpenStation(){
        // loop through station objects and check which are open
        Station s = new Station();
        int storedIndex = 0;
        int timeInTraffic = 9999;

        for(int i = 0; i < addressesReturned.size(); i++){
           s = getStationByAddress(addressesReturned.get(i));
           if(s.isTheStationOpen()){
               // take the station that is open and with the smallest traffic time
               if(timeInTraffic > timeToArriveInTraffic.get(i)){
                   timeInTraffic = timeToArriveInTraffic.get(i);
                   storedIndex = i;
               }
           }
        }
        return addressesReturned.get(storedIndex);

    }


    public String snmStringConstruct(String station){

        String[] indvidualWords = station.split("\\s+");
        Station currentStation = getStationByAddress(station);
       // String kms = " ";
        String timeInMinutes = " ";
        String openOrNot;
        String company = currentStation.getCompany();


        if(currentStation.isTheStationOpen()){
            openOrNot = "OPEN";
        }else{
            openOrNot = "CLOSED";
        }

        for(int i = 0; i < addressesReturned.size(); i ++){
            if(station.equals(addressesReturned.get(i))){
                //kms = distanceToStation.get(i);
                timeInMinutes = timeToStation.get(i);

            }
        }

        return currentStation.getSuburb() + " " + company + " | "+ timeInMinutes + " | " + openOrNot;
    }

    public Station getStationByAddress(String station) {
        return stations.get(stringSplitter(station));
    }

    public void setClosestStations(String Address){

        closestStations.add(getStationByAddress(Address));

    }

    public ArrayList<Station> getClosestStations() {
        return closestStations;
    }



}

