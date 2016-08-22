package com.gsoc.ijosa.liquidgalaxycontroller;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by RAFA on 22/05/2015.
 */
public class CategoriesAdapter extends CursorAdapter {

    private static final int CATEGORY_COLUMN_NAME = 1;


    CategoriesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.caterory_list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView poiName = (TextView) view.findViewById(R.id.category_list_item_textview);
        String showName = cursor.getString(CATEGORY_COLUMN_NAME);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            poiName.setTextSize(15);
        }
        poiName.setText(showName);

    }
}