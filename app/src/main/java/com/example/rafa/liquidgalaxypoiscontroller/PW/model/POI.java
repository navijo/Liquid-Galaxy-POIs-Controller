package com.example.rafa.liquidgalaxypoiscontroller.PW.model;

/**
 * Created by lgwork on 27/05/16.
 */
public class POI {

    public String name;
    public String description;
    public Point point;

    public POI() {
        this.name = "";
        this.point = new Point();
        this.description = "";
    }

    public POI(String name, String description, Point point) {
        this.name = name;
        this.description = description;
        this.point = point;
    }


    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
