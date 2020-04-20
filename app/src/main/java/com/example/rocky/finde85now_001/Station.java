package com.example.rocky.finde85now_001;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Station {

    //fields
    private double latitude;
    private double longitude;
    private int openingTime;
    private int closingTime;
    private boolean twentyFourHour;

    private String closestStationAddress;
    private String secondClosestStationAddress;
    private String thirdClosestStationAddress;

    private String company;
    private String suburb;
    private String fullAddress;
    private String currentTime;

    //constructor
    public Station(){

    }

    public Station(double latitude, double longitude, int openingTime, int closingTime, String suburb, String company, Boolean twentyFourHour){

        this.latitude = latitude;
        this.longitude = longitude;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.suburb = suburb;
        this.twentyFourHour = twentyFourHour;
        this.company = company;

    }

    //Methods

    public void setOpeningTime(int opening){

       openingTime = opening;

    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getThirdClosestStationAddress() {
        return thirdClosestStationAddress;
    }

    public void setThirdClosestStationAddress(String thirdClosestStationAddress) {
        this.thirdClosestStationAddress = thirdClosestStationAddress;
    }

    public String getSecondClosestStationAddress() {
        return secondClosestStationAddress;
    }

    public void setSecondClosestStationAddress(String secondClosestStationAddress) {
        this.secondClosestStationAddress = secondClosestStationAddress;
    }


    public String getClosestStationAddress() {
        return closestStationAddress;
    }

    public void setClosestStationAddress(String closestStationAddress) {
        this.closestStationAddress = closestStationAddress;
    }


    public int getOpeningTime(){

        return openingTime;

    }

    public void setClosingTime(int closing){

        closingTime = closing;

    }

    public int getClosingTime(){

        return closingTime;

    }

    public void setLatitude(double lat){

        latitude = lat;

    }

    public double getLatitude(){

        return latitude;

    }

    public void setLongitude(double lng){

        longitude = lng;

    }

    public double getLongitude(){

        return longitude;

    }

    public void setFullAddress(String address){

        fullAddress = address;

    }

    public String getFullAddress(){

        return fullAddress;

    }

    public void setSuburb(String suburb){

        this.suburb = suburb;

    }

    public String getSuburb(){

        return suburb;

    }

    public static String toMins(String time) {

        return time.replaceFirst(":", "");
    }

    public String getCurrentTime(){
        return currentTime;
    }

    public boolean isTheStationOpen() {
        // Get current user time
        currentTime = new SimpleDateFormat("kk:mm", Locale.getDefault()).format(new Date());
        Log.d("theCurrentTime", currentTime); // check if its working

        currentTime = toMins(currentTime);
        int timeNow = Integer.valueOf(currentTime);
        timeNow = 400;
        return timeNow > this.openingTime && timeNow < this.closingTime;
    }


}
