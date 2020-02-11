package com.example.rocky.finde85now_001;

public class Station {

    //fields
    private double latitude;
    private double longitude;
    private int openingTime;
    private int closingTime;

    private String fullAddress;

    //constructor
    public Station(double latitude, double longitude, int opens, int closes){

        this.latitude = latitude;
        this.longitude = longitude;
        this.openingTime = opens;
        this.closingTime = closes;

    }

    //Methods

    public void setOpeningTime(int opening){

       openingTime = opening;

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

}
