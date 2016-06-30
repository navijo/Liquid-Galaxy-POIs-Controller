package com.example.rafa.liquidgalaxypoiscontroller.advancedTools;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lgwork on 30/06/16.
 */
public class LGTask implements Parcelable {

    private long id;
    private String title;
    private String description;
    private String script;

    public LGTask() {
        id = 0l;
        title = "";
        script  = "";
        description = "";
    }

    public LGTask(long id,String title, String script,String description) {
        this.id = id;
        this.title = title;
        this.script = script;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LGTask task = (LGTask) o;

        return task.getId() == this.getId();

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + new Long(id).hashCode();
        return result;
    }

    public static final Creator CREATOR =
            new Creator() {
                public LGTask createFromParcel(Parcel in) {
                    return new LGTask(in);
                }

                public LGTask[] newArray(int size) {
                    return new LGTask[size];
                }
            };


    public LGTask(Parcel in) {
        id = in.readLong();
        title = in.readString();
        description = in.readString();
        script = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(script);
    }
}
