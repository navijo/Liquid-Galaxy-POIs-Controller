package com.example.rafa.liquidgalaxypoiscontroller;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.rafa.liquidgalaxypoiscontroller.R;

/**
 * Created by RAFA on 07/06/2015.
 */
public class POIsAdapter extends CursorAdapter{

    //FIXME: Changed from 3 to 1 in order to display the poi Name
    public static final int POI_COLUMN_VISITED_PLACE_NAME = 1;

    public static final int TOUR_COLUMN_NAME = 1;
    public static final int CATEGORY_COLUMN_NAME = 1;

    private String itemName;


    public POIsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.poi_list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView poiName = (TextView) view.findViewById(R.id.poi_list_item_textview);
        if(itemName != null) {
            if (itemName.equals("POI")) {
                poiName.setText(cursor.getString(POI_COLUMN_VISITED_PLACE_NAME));
            } else if (itemName.equals("TOUR")) {
                poiName.setText(cursor.getString(TOUR_COLUMN_NAME));
            } else {
                poiName.setText(cursor.getString(CATEGORY_COLUMN_NAME));
            }
        }
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}