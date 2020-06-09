package com.example.rocky.finde85now_001;

import android.location.Location;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.rocky.finde85now_001.Station.toMins;

class StationHandler {


    // Arrays to hold station lists
    private ArrayList<Station> stations = new ArrayList<>();
    private ArrayList<Station> shortlistedDestinations = new ArrayList<>();
    private double[] distance = new double[24];
    private static String locationsToSend = "";

    public ArrayList<String> addressesReturned = new ArrayList<>();
    public ArrayList<Integer> timeToArriveInTraffic = new ArrayList<>();
    public ArrayList<String> distanceToStation = new ArrayList<>();
    public ArrayList<String> timeToStation = new ArrayList<>();

    public static String getLocationsToSend() {
        return locationsToSend;
    }


    void initialiseStations() {
        stations.add(new Station(-33.649917, 150.862685, 530, 2130, "Vineyard", "Freedom", false));
        stations.add(new Station(-33.810202, 151.032491, 600, 2200, "Rydalmere", "United", false));
        stations.add(new Station(-33.861967, 151.167653, 0,   2400, "Rozelle","United",  true));
        stations.add(new Station(-33.901910, 151.208229, 0,   2400, "Waterloo","United", true));
        stations.add(new Station(-33.901877, 151.037178, 500, 2400, "Yagoona","United", false));
        stations.add(new Station(-33.755790, 151.282715, 500, 2400, "Dee Why","United", false));
        stations.add(new Station(-33.746039, 150.622454, 600, 2100, "Blaxland","United", false));
        stations.add(new Station(-33.899258, 151.036924, 600, 2200, "Yagoona1","United", false));
        stations.add(new Station(-33.872234, 150.900077, 600, 2200, "Prairiewood","United", false));
        stations.add(new Station(-34.030073, 150.831892, 0,   2400, "Minto","United", true));
        stations.add(new Station(-33.680160, 151.225010, 700, 2200, "Terrey Hills","United", false));
    }

    // Parallel method trying to get same result using station class instead of hard coded array
    void getDistanceBetween(double userLocationLatitude, double userLocationLongitude) {
        float[] straightLineDistanceInMeters = new float[1];

        for (int i = 0; i < stations.size(); i++) {
            Location.distanceBetween(stations.get(i).getLatitude(), stations.get(i).getLongitude(), userLocationLatitude, userLocationLongitude, straightLineDistanceInMeters);

            distance[i] = straightLineDistanceInMeters[0];

            // store sub 30km stations in a straight line
            if (straightLineDistanceInMeters[0] < 9000) {
                shortlistedDestinations.add(stations.get(i));
            }

            String distanceInStraightLine = Double.toString(distance[i]);
            Log.d("distanceInStraightLine", distanceInStraightLine);
        }
        stringConstructor();
    }

    private int stringSplitter(String closestE85Station) {
        int stationIndex = 0;
        String suburb = "";

        // take only the suburb from the output
        int commaIndex = closestE85Station.indexOf(',');
        String street = closestE85Station.substring(0, commaIndex);
        String restOfWord = closestE85Station.substring(commaIndex + 2);
        int d = restOfWord.indexOf(" ");

        String nextWordCheck = restOfWord.substring(d + 1, d + 3); // grab the next 2 characters after the space
        boolean hasLowerCase = !nextWordCheck.equals(nextWordCheck.toUpperCase()); // Check if the 2 letters are upper case or not

        if (hasLowerCase) { // if they are not the next word must be part of the suburb
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
            if (suburb.equals(stationSuburb)) { // stations match! grab that stations index
                return i;
            }
        }
        return stationIndex;
    }


    private void stringConstructor() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < shortlistedDestinations.size(); i++) {

            if (i == shortlistedDestinations.size() - 1) {
                sb.append(shortlistedDestinations.get(i).getLatitude()).append(",");
                sb.append(shortlistedDestinations.get(i).getLongitude());
                break;
            }
            sb.append(shortlistedDestinations.get(i).getLatitude()).append(",");
            sb.append(shortlistedDestinations.get(i).getLongitude()).append("|");

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
        String kms = " ";
        String timeInMinutes = " ";
        String openOrNot;

        Station currentStation = getStationByAddress(station);

        if(currentStation.isTheStationOpen()){
            openOrNot = "OPEN";
        }else{
            openOrNot = "CLOSED";
        }

        for(int i = 0; i < addressesReturned.size(); i ++){
            if(station.equals(addressesReturned.get(i))){
                kms = distanceToStation.get(i);
                timeInMinutes = timeToStation.get(i);
            }
        }

        return indvidualWords[0] + " " + indvidualWords[1] + " " + indvidualWords[2] + " " + indvidualWords[3] + " " + kms + " " + timeInMinutes + " " + openOrNot;
    }

    public Station getStationByAddress(String station) {
        return stations.get(stringSplitter(station));
    }

    public String getStationBySuburb(String station){
        return stations.get(stringSplitter(station)).getSuburb();
    }

}

