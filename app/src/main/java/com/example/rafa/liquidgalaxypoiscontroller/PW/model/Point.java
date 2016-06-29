package com.example.rafa.liquidgalaxypoiscontroller.PW.model;

/**
 * Created by lgwork on 27/05/16.
 */
public class Point {


    public String latitude;
    public String longitude;

    public Point() {
        this.latitude = "";
        this.longitude = "";
    }

    public Point(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
