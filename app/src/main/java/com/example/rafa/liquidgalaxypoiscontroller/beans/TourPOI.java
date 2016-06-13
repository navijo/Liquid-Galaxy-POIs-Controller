package com.example.rafa.liquidgalaxypoiscontroller.beans;

public class TourPOI {
    private int order;
    private int poiID;
    private int tourID;

    public TourPOI(int tourID, int poiID, int order) {
        this.tourID = tourID;
        this.poiID = poiID;
        this.order = order;
    }

    public int getTourID() {
        return this.tourID;
    }

    public void setTourID(int tourID) {
        this.tourID = tourID;
    }

    public int getPoiID() {
        return this.poiID;
    }

    public void setPoiID(int poiID) {
        this.poiID = poiID;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String toString() {
        return "TourPOI{tourID=" + this.tourID + ", poiID=" + this.poiID + ", order=" + this.order + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TourPOI)) {
            return false;
        }
        TourPOI tourPOI = (TourPOI) o;
        if (this.order != tourPOI.order) {
            return false;
        }
        if (this.poiID != tourPOI.poiID) {
            return false;
        }
        if (this.tourID != tourPOI.tourID) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((this.tourID * 31) + this.poiID) * 31) + this.order;
    }
}
