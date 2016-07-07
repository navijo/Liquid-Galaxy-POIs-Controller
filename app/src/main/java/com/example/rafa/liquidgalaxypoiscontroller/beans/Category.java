package com.example.rafa.liquidgalaxypoiscontroller.beans;

public class Category {
    private int fatherID;
    private String hide;
    private int id;
    private String name;
    private String shownName;

    public Category() {
        super();
    }

    public Category(int id, String name, int fatherID, String shownName, String hide) {
        this.id = id;
        this.name = name;
        this.fatherID = fatherID;
        this.shownName = shownName;
        this.hide = hide;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFatherID() {
        return this.fatherID;
    }

    public void setFatherID(int fatherID) {
        this.fatherID = fatherID;
    }

    public String getShownName() {
        return this.shownName;
    }

    public void setShownName(String shownName) {
        this.shownName = shownName;
    }

    public String getHide() {
        return this.hide;
    }

    public void setHide(String hide) {
        this.hide = hide;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category)) {
            return false;
        }
        Category category = (Category) o;
        if (this.fatherID != category.fatherID) {
            return false;
        }
        if (this.id != category.id) {
            return false;
        }
        if (!this.hide.equals(category.hide)) {
            return false;
        }
        if (!this.name.equals(category.name)) {
            return false;
        }
        return this.shownName.equals(category.shownName);
    }

    public int hashCode() {
        return (((((((this.id * 31) + this.name.hashCode()) * 31) + this.fatherID) * 31) + this.shownName.hashCode()) * 31) + this.hide.hashCode();
    }

    public String toString() {
        return "Category{id=" + this.id + ", name='" + this.name + '\'' + ", fatherID=" + this.fatherID + ", shownName='" + this.shownName + '\'' + ", hide='" + this.hide + '\'' + '}';
    }
}
