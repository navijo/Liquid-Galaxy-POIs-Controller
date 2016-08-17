package com.example.rafa.liquidgalaxypoiscontroller.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.beans.Tour;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ivan Josa on 7/07/16.
 */
public class ToursGridViewAdapter extends BaseAdapter {

    List<Tour> tourList;
    Context context;
    FragmentActivity activity;
    Session session;


    public ToursGridViewAdapter(List<Tour> tourList, Context context, FragmentActivity activity) {
        this.tourList = tourList;
        this.context = context;
        this.activity = activity;

        GetSessionTask getSessionTask = new GetSessionTask();
        getSessionTask.execute();
    }

    @Override
    public int getCount() {
        return this.tourList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.tourList.get(i);

    }

    @Override
    public long getItemId(int i) {
        return this.tourList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Tour currentTour = this.tourList.get(i);

        Button button = new Button(context);
        String displayName = currentTour.getName();
        button.setText(displayName);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            button.setTextSize(15);
        }

        Drawable top = context.getResources().getDrawable(R.drawable.politour48);
        button.setCompoundDrawablesWithIntrinsicBounds(top, null, null, null);

        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        button.setMaxLines(1);


        button.setBackground(context.getResources().getDrawable(R.drawable.button_rounded_grey));
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchTourTask tourTask = new LaunchTourTask(currentTour);
                tourTask.execute();
            }
        });

        return button;
    }


    private class LaunchTourTask extends AsyncTask<Void, Void, Boolean> {

        Tour currentTour;
        private ProgressDialog dialog;


        public LaunchTourTask(Tour currentTour) {
            this.currentTour = currentTour;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(context);
                String message = context.getResources().getString(R.string.viewing) + " " + this.currentTour.getName() + " " + context.getResources().getString(R.string.inLG);
                dialog.setMessage(message);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.stop_tour), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancel(true);
                    }
                });
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                dialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            List<HashMap<String, String>> pois = new ArrayList<>();
            List<Integer> poisDuration = new ArrayList<Integer>();

            try {
                Cursor tourPoiCursor = POIsContract.TourPOIsEntry.getPOIsByTourID(String.valueOf(this.currentTour.getId()));
                while (tourPoiCursor.moveToNext()) {
                    int poiID = tourPoiCursor.getInt(tourPoiCursor.getColumnIndex(POIsContract.TourPOIsEntry.COLUMN_POI_ID));
                    poisDuration.add(tourPoiCursor.getInt(tourPoiCursor.getColumnIndex(POIsContract.TourPOIsEntry.COLUMN_POI_DURATION)));
                    pois.add(getPOIData(poiID));
                }
                try {
                    sendTourPOIs(pois, poisDuration);
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
            } catch (Exception e2) {
                return false;
            }
        }

        private void sendTourPOIs(List<HashMap<String, String>> pois, List<Integer> poisDuration) throws IOException, JSchException {
            sendFirstTourPOI(pois.get(0));
            sendOtherTourPOIs(pois, poisDuration);
        }

        private void sendOtherTourPOIs(List<HashMap<String, String>> pois, List<Integer> poisDuration) throws IOException, JSchException {
            int i = 1;
            while (!isCancelled()) {
                sendTourPOI(poisDuration.get(i), buildCommand(pois.get(i)));
                i++;
                if (i == pois.size()) {
                    i = 0;
                }
            }
        }

        private void sendFirstTourPOI(HashMap<String, String> firstPoi) throws IOException, JSchException {
            LGUtils.setConnectionWithLiquidGalaxy(session, buildCommand(firstPoi), activity);
        }

        private void sendTourPOI(Integer duration, String command) throws IOException, JSchException {
            try {
                Thread.sleep((long) ((duration * 2) * 1000));
                LGUtils.setConnectionWithLiquidGalaxy(session, command, activity);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        private HashMap<String, String> getPOIData(int id) throws Exception {
            Cursor c = POIsContract.POIEntry.getPOIByID(activity, String.valueOf(id));
            HashMap<String, String> poi = new HashMap<>();
            if (c.moveToNext()) {
                poi.put("completeName", c.getString(c.getColumnIndex(POIsContract.POIEntry.COLUMN_COMPLETE_NAME)));
                poi.put("longitude", String.valueOf(c.getFloat(c.getColumnIndex(POIsContract.POIEntry.COLUMN_LONGITUDE))));
                poi.put("latitude", String.valueOf(c.getFloat(c.getColumnIndex(POIsContract.POIEntry.COLUMN_LATITUDE))));
                poi.put("altitude", String.valueOf(c.getFloat(c.getColumnIndex(POIsContract.POIEntry.COLUMN_ALTITUDE))));
                poi.put("heading", String.valueOf(c.getFloat(c.getColumnIndex(POIsContract.POIEntry.COLUMN_HEADING))));
                poi.put("tilt", String.valueOf(c.getFloat(c.getColumnIndex(POIsContract.POIEntry.COLUMN_TILT))));
                poi.put("range", String.valueOf(c.getFloat(c.getColumnIndex(POIsContract.POIEntry.COLUMN_RANGE))));
                poi.put("altitudeMode", c.getString(c.getColumnIndex(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE)));
                return poi;
            }
            throw new Exception("There is no POI with this features inside the data base. Try creating once correct.");
        }

        private String buildCommand(HashMap<String, String> poi) {
            return "echo 'flytoview=<gx:duration>5</gx:duration><LookAt><longitude>" + poi.get("longitude") + "</longitude><latitude>" + poi.get("latitude") + "</latitude><altitude>" + poi.get("altitude") + "</altitude><heading>" + poi.get("heading") + "</heading><tilt>" + poi.get("tilt") + "</tilt><range>" + poi.get("range") + "</range><gx:altitudeMode>" + poi.get("altitudeMode") + "</gx:altitudeMode></LookAt>' > /tmp/query.txt";
        }


        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            resetTourSettings();
            if (!success) {
                AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);

                // set the message to display
                alertbox.setTitle("Error");
                alertbox.setMessage("There's probably no POI inside this Tour");

                // set a positive/yes button and create a listener
                alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                alertbox.show();
            }
            if (dialog != null) {
                dialog.hide();
                dialog.dismiss();
            }
        }

        public void resetTourSettings() {
            this.cancel(true);
            showStopAlert();
        }


        private void showStopAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("The tour running on LG has been stopped.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }


    private class GetSessionTask extends AsyncTask<Void, Void, Void> {

        public GetSessionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            session = LGUtils.getSession(activity);
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }


}
