package com.example.rafa.liquidgalaxypoiscontroller;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.beans.TourPOI;

import java.util.List;

/**
 * Created by RAFA on 18/06/2015.
 */
public class TourPOIsAdapter extends BaseAdapter {

    private static List<TourPOI> pois;

    private static FragmentActivity activity;
    private static String type = "creating";
    private static int global_interval = 0;


    public TourPOIsAdapter(FragmentActivity activity, List<TourPOI> tourPOIs) {
        TourPOIsAdapter.activity = activity;
        pois = tourPOIs;
    }

    public static void setType(String t) {
        type = t;
    }

    public static int getGlobalInterval() {
        return global_interval;
    }

    public static void setGlobalInterval(int globalInterval) {
        global_interval = globalInterval;
    }

    private static void screenSizeTreatment(View view, TextView poi) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;


        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth >= 1000) {
            ImageView down = (ImageView) view.findViewById(R.id.move_down);
            ImageView up = (ImageView) view.findViewById(R.id.move_up);

            down.setImageResource(R.drawable.ic_keyboard_arrow_down_black_36dp);
            up.setImageResource(R.drawable.ic_keyboard_arrow_up_black_36dp);
            poi.setTextSize(24);
        } else if (smallestWidth > 720 && smallestWidth < 1000) {
            poi.setTextSize(22);
        } else if (smallestWidth <= 720 && smallestWidth >= 600) {
            poi.setTextSize(20);
        } else if (smallestWidth < 600 && smallestWidth >= 500) {
            poi.setTextSize(16);
        }
    }

    private static boolean isNumeric(String str) {
        try {
            int d = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

//    public static void deleteDurationByPosition(int durationIndex) {
//        poisDuration.remove(durationIndex);
//    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return pois.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) throws ArrayIndexOutOfBoundsException{

        if(position < 0){
            throw new ArrayIndexOutOfBoundsException("You can't move this item up, try to move it down.");
        }else if(position > pois.size()-1){
            throw new ArrayIndexOutOfBoundsException("You can't move this item down, try to move it up");
        }
        return pois.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        TourPOI tourPOI = (TourPOI) getItem(position);

        if(convertView == null){
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.tour_pois_list_item, parent, false);
        }

        //we get the poi name
        TextView name = (TextView) view.findViewById(R.id.poi_complete_name);
        name.setText(tourPOI.getPoiName());

        //we get the POI field called Seconds and we set its behaviour when user types on it.
        EditText seconds = (EditText) view.findViewById(R.id.poi_seconds);
        seconds.setText(String.valueOf(tourPOI.getDuration()));
        secondsBehaviour(tourPOI, seconds, position);

        setArrowsBehaviour(view, position, name);
        setDeleteItemButtonBehaviour(view, tourPOI);

        return view;
    }

    private void secondsBehaviour(final TourPOI tourPOI, final EditText seconds, final int position) {
        seconds.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int s = 0;
                if (!hasFocus) {
                    String sec = seconds.getText().toString();
                    if (isNumeric(sec)) {
                        s = Integer.parseInt(sec);

                        tourPOI.setDuration(s);
                    }
                }
            }
        });
    }

    private void setDeleteItemButtonBehaviour(View view, TourPOI tourPOI) {
        if(type.equals("creating")) {
            CreateItemFragment.deleteButtonTreatment(view, tourPOI);
        }else{
            UpdateItemFragment.deleteButtonTreatment(view, tourPOI);
        }
    }

    private void setArrowsBehaviour(View view, final int position, TextView poi) {

        screenSizeTreatment(view, poi);
        view.findViewById(R.id.move_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDown(position);
            }
        });

        view.findViewById(R.id.move_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveUp(position);
            }
        });
    }

    private void moveUp(int position){
        TourPOI toMoveUp;
        TourPOI toMoveDown;
        try {
            toMoveUp = (TourPOI) getItem(position);
            toMoveDown = (TourPOI) getItem(position - 1);
            pois.remove(position);
            pois.remove(position - 1);
            toMoveUp.setOrder(position - 1);
            pois.add(position - 1, toMoveUp);
            toMoveDown.setOrder(position);
            pois.add(position, toMoveDown);
            notifyDataSetChanged();
        }catch (ArrayIndexOutOfBoundsException ex){
            Toast.makeText(activity, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void moveDown(int position){
        try {
            TourPOI toMoveDown = (TourPOI) getItem(position);
            TourPOI toMoveUp = (TourPOI) getItem(position + 1);

            pois.remove(position + 1);
            pois.remove(position);
            toMoveUp.setOrder(position);
            pois.add(position, toMoveUp);
            toMoveDown.setOrder(position + 1);
            pois.add(position + 1, toMoveDown);

            notifyDataSetChanged();
        }catch (ArrayIndexOutOfBoundsException ex){
            Toast.makeText(activity, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

    }
}