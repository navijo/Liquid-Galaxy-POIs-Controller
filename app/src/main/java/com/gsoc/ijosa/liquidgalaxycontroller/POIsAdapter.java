package com.gsoc.ijosa.liquidgalaxycontroller;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by RAFA on 07/06/2015.
 */
class POIsAdapter extends CursorAdapter {

    private static final int POI_COLUMN_VISITED_PLACE_NAME = 1;

    private static final int TOUR_COLUMN_NAME = 1;
    private static final int CATEGORY_COLUMN_NAME = 1;

    private String itemName;


    POIsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.poi_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView poiName = (TextView) view.findViewById(R.id.poi_list_item_textview);
        if(itemName != null) {
            switch (itemName) {
                case "POI":
                    poiName.setText(cursor.getString(POI_COLUMN_VISITED_PLACE_NAME));
                    break;
                case "TOUR":
                    poiName.setText(cursor.getString(TOUR_COLUMN_NAME));
                    break;
                default:
                    poiName.setText(cursor.getString(CATEGORY_COLUMN_NAME));
                    break;
            }
        }
    }

    void setItemName(String itemName) {
        this.itemName = itemName;
    }

}