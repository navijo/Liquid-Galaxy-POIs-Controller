package com.example.rafa.liquidgalaxypoiscontroller;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.List;

public class StableArrayAdapter extends ArrayAdapter<String> {
    final int INVALID_ID;
    HashMap<String, Integer> mIdMap;

    public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        this.INVALID_ID = -1;
        this.mIdMap = new HashMap();
        for (int i = 0; i < objects.size(); i++) {
            this.mIdMap.put(objects.get(i), Integer.valueOf(i));
        }
    }

    public long getItemId(int position) {
        if (position < 0 || position >= this.mIdMap.size()) {
            return -1;
        }
        return (long) this.mIdMap.get(getItem(position)).intValue();
    }

    public boolean hasStableIds() {
        return true;
    }
}
