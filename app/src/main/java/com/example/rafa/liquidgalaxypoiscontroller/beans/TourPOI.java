package com.example.rafa.liquidgalaxypoiscontroller.beans;

/**
 * Created by RAFA on 11/06/2015.
 */
public class TourPOI {

    private int tourID;
    private int poiID;
    private int order;

    public TourPOI(){}
    public TourPOI(int tourID, int poiID, int order){
        this.tourID = tourID;
        this.poiID = poiID;
        this.order = order;
    }

    public int getTourID() {
        return tourID;
    }

    public void setTourID(int tourID) {
        this.tourID = tourID;
    }

    public int getPoiID() {
        return poiID;
    }

    public void setPoiID(int poiID) {
        this.poiID = poiID;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "TourPOI{" +
                "tourID=" + tourID +
                ", poiID=" + poiID +
                ", order=" + order +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TourPOI)) return false;

        TourPOI tourPOI = (TourPOI) o;

        if (order != tourPOI.order) return false;
        if (poiID != tourPOI.poiID) return false;
        if (tourID != tourPOI.tourID) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tourID;
        result = 31 * result + poiID;
        result = 31 * result + order;
        return result;
    }
}
