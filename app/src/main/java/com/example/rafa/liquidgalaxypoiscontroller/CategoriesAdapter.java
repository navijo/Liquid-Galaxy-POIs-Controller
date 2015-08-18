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
 * Created by RAFA on 22/05/2015.
 * Adapter for Categories list items.
 */
public class CategoriesAdapter extends CursorAdapter {

    private static final int CATEGORY_COLUMN_NAME = 1;
    private static final int CATEGORY_COLUMN_HIDE = 4;

    public CategoriesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.caterory_list_item, parent, false);
        return view;
    }

    //Depending on the screen size, the POIs list items will change their text name size.
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

        TextView poiName = (TextView) view.findViewById(R.id.category_list_item_textview);
        screenSizeTreatment(view, poiName);
        //If admin user decides to hide one item, it will not appears in the user page, but in the
        //admin page it will appear coulored in red. If one item is not hide, it's color is a mix
        //between blue and green, which is called 'accent material light' color.
        poiName.setTextColor(context.getResources().getColor(R.color.accent_material_light));
        if(cursor.getInt(CATEGORY_COLUMN_HIDE) == 1){
            poiName.setTextColor(context.getResources().getColor(R.color.red));
        }
        poiName.setText(cursor.getString(CATEGORY_COLUMN_NAME));
    }
}
