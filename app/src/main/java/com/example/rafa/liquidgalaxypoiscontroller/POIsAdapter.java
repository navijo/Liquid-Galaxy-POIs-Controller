package com.example.rafa.liquidgalaxypoiscontroller;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by RAFA on 07/06/2015.
 */
public class POIsAdapter extends CursorAdapter{

    public static final int POI_COLUMN_NAME = 1;
    public static final int TOUR_COLUMN_NAME = 1;
    public static final int CATEGORY_COLUMN_NAME = 1;
    private static final int POI_COLUMN_HIDE= 10;
    private static final int TOUR_COLUMN_HIDE = 3;

    private String itemName;


    public POIsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.poi_list_item, parent, false);
        return view;
    }
    private void screenSizeTreatment(View view, TextView poi) {
        DisplayMetrics metrics = new DisplayMetrics();
        FragmentActivity x = (FragmentActivity) view.getContext();
        x.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;

        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth >= 1000) {
            poi.setTextSize(25);
        } else if(smallestWidth >720 && smallestWidth<1000){
            poi.setTextSize(23);
        } else if(smallestWidth <= 720 && smallestWidth >= 600 ){
            poi.setTextSize(21);
        } else if(smallestWidth < 600 && smallestWidth >= 500 ){
            poi.setTextSize(16);
        }

    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView poiName = (TextView) view.findViewById(R.id.poi_list_item_textview);
        screenSizeTreatment(view, poiName);
        poiName.setTextColor(context.getResources().getColor(R.color.accent_material_light));
        if(itemName != null) {
            if (itemName.equals("POI")) {
                poiName.setText(cursor.getString(POI_COLUMN_NAME));
                if(cursor.getInt(POI_COLUMN_HIDE) == 1){
                    poiName.setTextColor(context.getResources().getColor(R.color.red));
                }
            } else if (itemName.equals("TOUR")) {
                poiName.setText(cursor.getString(TOUR_COLUMN_NAME));
                if(cursor.getInt(TOUR_COLUMN_HIDE) == 1){
                    poiName.setTextColor(context.getResources().getColor(R.color.red));
                }
            } else {
                poiName.setText(cursor.getString(CATEGORY_COLUMN_NAME));
            }
        }
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
