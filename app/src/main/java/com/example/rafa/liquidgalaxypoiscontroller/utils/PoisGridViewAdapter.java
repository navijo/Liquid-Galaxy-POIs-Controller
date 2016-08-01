package com.example.rafa.liquidgalaxypoiscontroller.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.beans.POI;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.List;

/**
 * Created by Ivan Josa on 7/07/16.
 */
public class PoisGridViewAdapter extends BaseAdapter {

    List<POI> poiList;
    Context context;
    Activity activity;
    Session session;

    public PoisGridViewAdapter(List<POI> poiList, Context context, Activity activity) {
        this.poiList = poiList;
        this.context = context;
        this.activity = activity;

        GetSessionTask getSessionTask = new GetSessionTask();
        getSessionTask.execute();
    }

    @Override
    public int getCount() {
        return this.poiList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.poiList.get(i);

    }

    @Override
    public long getItemId(int i) {
        return this.poiList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final POI currentPoi = this.poiList.get(i);

        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);

        RelativeLayout layout = new RelativeLayout(context);
        layout.setBackground(context.getResources().getDrawable(R.drawable.button_rounded_grey));
        layout.setLayoutParams(params);

        //View POI
        RelativeLayout.LayoutParams paramsView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ImageButton viewPoiButton = new ImageButton(context);
        paramsView.addRule(RelativeLayout.CENTER_VERTICAL);
        paramsView.addRule(RelativeLayout.ALIGN_START);
        viewPoiButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_place_black_24dp));
        viewPoiButton.setBackground(context.getResources().getDrawable(R.drawable.button_rounded_grey));
        viewPoiButton.setLayoutParams(paramsView);
        viewPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = buildCommand(currentPoi);
                VisitPoiTask visitPoiTask = new VisitPoiTask(command, currentPoi, false);
                visitPoiTask.execute();
            }
        });
        layout.addView(viewPoiButton);

        int maxLengthPoiName = getMaxLength();

        //Poi Name
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        TextView poiName = new TextView(context);
        if (currentPoi.getName().length() > maxLengthPoiName) {
            String name = currentPoi.getName().substring(0, maxLengthPoiName) + "...";
            poiName.setText(name);
        } else {
            poiName.setText(currentPoi.getName());
        }

        poiName.setTextSize(20);
        poiName.setMaxLines(2);
        paramsText.addRule(RelativeLayout.CENTER_IN_PARENT);

        poiName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = buildCommand(currentPoi);
                VisitPoiTask visitPoiTask = new VisitPoiTask(command, currentPoi, false);
                visitPoiTask.execute();
            }
        });

        layout.addView(poiName);
        poiName.setLayoutParams(paramsText);

        //Rotation Button
        RelativeLayout.LayoutParams paramsRotate = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ImageButton rotatePoiButton = new ImageButton(context);
        rotatePoiButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_autorenew_black_36dp));
        rotatePoiButton.setBackground(context.getResources().getDrawable(R.drawable.button_rounded_grey));
        paramsRotate.addRule(RelativeLayout.ALIGN_PARENT_END);

        rotatePoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = buildCommand(currentPoi);
                VisitPoiTask visitPoiTask = new VisitPoiTask(command, currentPoi, true);
                visitPoiTask.execute();
            }
        });

        layout.addView(rotatePoiButton);
        rotatePoiButton.setLayoutParams(paramsRotate);

        return layout;
    }

    private int getMaxLength() {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;


        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth > 800) {
            return 35;
        } else if (smallestWidth <= 800 && smallestWidth >= 600) {
            return 20;
        } else {
            return 15;
        }
    }


    private String buildCommand(POI poi) {

        return "echo 'flytoview=<gx:duration>6</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt><longitude>" + poi.getLongitude() + "</longitude>" +
                "<latitude>" + poi.getLatitude() + "</latitude>" +
                "<altitude>" + poi.getAltitude() + "</altitude>" +
                "<heading>" + poi.getHeading() + "</heading>" +
                "<tilt>" + poi.getTilt() + "</tilt>" +
                "<range>" + poi.getRange() + "</range>" +
                "<gx:altitudeMode>" + poi.getAltitudeMode() + "</gx:altitudeMode>" +
                "</LookAt>' > /tmp/query.txt";
    }


    private class VisitPoiTask extends AsyncTask<Void, Void, String> {

        String command;
        POI currentPoi;
        boolean rotate;
        int rotationAngle = 10;
        int rotationFactor = 1;
        boolean changeVelocity = false;
        private ProgressDialog dialog;

        public VisitPoiTask(String command, POI currentPoi, boolean rotate) {
            this.command = command;
            this.currentPoi = currentPoi;
            this.rotate = rotate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(context);
                String message = context.getResources().getString(R.string.viewing) + " " + this.currentPoi.getName() + " " + context.getResources().getString(R.string.inLG);
                dialog.setMessage(message);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);


                //Buton positive => more speed
                //Button neutral => less speed
                if (this.rotate) {
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.speedx2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing, we after define the onclick
                        }
                    });

                    dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getResources().getString(R.string.speeddiv2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing, we after define the onclick
                        }
                    });
                }


                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fast_forward_black_36dp, 0, 0);
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fast_rewind_black_36dp, 0, 0);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeVelocity = true;
                        rotationFactor = rotationFactor * 2;

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(context.getResources().getString(R.string.speedx4));
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(context.getResources().getString(R.string.speeddiv2));
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fast_rewind_black_36dp, 0, 0);

                        if (rotationFactor == 4) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                        }
                    }
                });
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeVelocity = true;
                        rotationFactor = rotationFactor / 2;

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(context.getResources().getString(R.string.speedx2));
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(context.getResources().getString(R.string.speeddiv4));
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fast_forward_black_36dp, 0, 0);

                        if (rotationFactor == 1) {
                            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
                        }
                    }
                });
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                session = LGUtils.getSession(activity);

                //We fly to the point
                LGUtils.setConnectionWithLiquidGalaxy(session, command, activity);


                //If rotation button is pressed, we start the rotation
                if (this.rotate) {
                    Thread.sleep(14000);

                    boolean isFirst = true;

                    while (!isCancelled()) {
                        session.sendKeepAliveMsg();

                        for (int i = 0; i <= (360 - this.currentPoi.getHeading()); i += (this.rotationAngle * this.rotationFactor)) {

                            String commandRotate = "echo 'flytoview=<gx:duration>3</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt>" +
                                    "<longitude>" + this.currentPoi.getLongitude() + "</longitude>" +
                                    "<latitude>" + this.currentPoi.getLatitude() + "</latitude>" +
                                    "<altitude>" + this.currentPoi.getAltitude() + "</altitude>" +
                                    "<heading>" + (this.currentPoi.getHeading() + i) + "</heading>" +
                                    "<tilt>" + this.currentPoi.getTilt() + "</tilt>" +
                                    "<range>" + this.currentPoi.getRange() + "</range>" +
                                    "<gx:altitudeMode>" + this.currentPoi.getAltitudeMode() + "</gx:altitudeMode>" +
                                    "</LookAt>' > /tmp/query.txt";


                            LGUtils.setConnectionWithLiquidGalaxy(session, commandRotate, activity);
                            session.sendKeepAliveMsg();

                            if (isFirst) {
                                isFirst = false;
                                Thread.sleep(6000);
                            } else {
                                Thread.sleep(4000);
                            }
                        }
                    }
                }

                return "";

            } catch (JSchException e) {
                this.cancel(true);
                if (dialog != null) {
                    dialog.dismiss();
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, context.getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            } catch (InterruptedException e) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, context.getResources().getString(R.string.visualizationCanceled), Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String success) {
            super.onPostExecute(success);
            if (success != null) {
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
            }
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
