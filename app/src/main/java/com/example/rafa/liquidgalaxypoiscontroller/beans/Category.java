package com.example.rafa.liquidgalaxypoiscontroller.beans;

/**
 * Created by RAFA on 26/05/2015.
 */
public class Category {

    private int id;
    private String name;
    private int fatherID;
    private String shownName;
    private String hide;

    public Category(int id, String name, int fatherID, String shownName, String hide) {
        this.id = id;
        this.name = name;
        this.fatherID = fatherID;
        this.shownName = shownName;
        this.hide = hide;
    }
    public Category(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFatherID() {
        return fatherID;
    }

    public void setFatherID(int fatherID) {
        this.fatherID = fatherID;
    }

    public String getShownName() {
        return shownName;
    }

    public void setShownName(String shownName) {
        this.shownName = shownName;
    }

    public String getHide() {
        return hide;
    }

    public void setHide(String hide) {
        this.hide = hide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category category = (Category) o;

        if (fatherID != category.fatherID) return false;
        if (id != category.id) return false;
        if (!hide.equals(category.hide)) return false;
        if (!name.equals(category.name)) return false;
        if (!shownName.equals(category.shownName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + fatherID;
        result = 31 * result + shownName.hashCode();
        result = 31 * result + hide.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fatherID=" + fatherID +
                ", shownName='" + shownName + '\'' +
                ", hide='" + hide + '\'' +
                '}';
    }
}
