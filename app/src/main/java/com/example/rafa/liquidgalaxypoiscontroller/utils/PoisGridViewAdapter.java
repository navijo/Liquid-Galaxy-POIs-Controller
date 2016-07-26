package com.example.rafa.liquidgalaxypoiscontroller.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
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
    private LayoutInflater mInflater;

    public PoisGridViewAdapter(List<POI> poiList, Context context, Activity activity) {
        this.poiList = poiList;
        this.context = context;
        this.activity = activity;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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


        // RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        TextView poiName = new TextView(context);
        if (currentPoi.getName().length() > 20) {
            String name = currentPoi.getName().substring(0, 20) + "...";
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


        RelativeLayout.LayoutParams paramsRotate = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ImageButton rotatePoiButton = new ImageButton(context);
        rotatePoiButton.setImageDrawable(context.getResources().getDrawable(R.drawable.rotate_36));
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

//
//        Button button = new Button(context);
//        String displayName = currentPoi.getName();
//        button.setText(displayName);
//
//        Drawable top = context.getResources().getDrawable(R.drawable.ic_place_black_24dp);
//        button.setCompoundDrawablesWithIntrinsicBounds(top, null, null, null);
//
//        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
//        button.setMaxLines(1);
//
//        button.setBackground(context.getResources().getDrawable(R.drawable.button_rounded_grey));
//        button.setLayoutParams(params);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String command = buildCommand(currentPoi);
//                VisitPoiTask visitPoiTask = new VisitPoiTask(command, currentPoi);
//                visitPoiTask.execute();
//            }
//        });
//
//        return button;

        return layout;
    }


    private String buildCommand(POI poi) {

        return "echo 'flytoview=<gx:duration>9</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt><longitude>" + poi.getLongitude() +
                "</longitude><latitude>" + poi.getLatitude() +
                "</latitude><altitude>" + poi.getAltitude() +
                "</altitude><heading>" + poi.getHeading() +
                "</heading><tilt>" + poi.getTilt() +
                "</tilt><range>" + poi.getRange() +
                "</range><gx:altitudeMode>" + poi.getAltitudeMode() +
                "</gx:altitudeMode></LookAt>' > /tmp/query.txt";
    }

    private class VisitPoiTask extends AsyncTask<Void, Void, String> {

        String command;
        POI currentPoi;
        boolean rotate;
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
                dialog.setCancelable(true);
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
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                LGUtils.setConnectionWithLiquidGalaxy(session, command, activity);

                if (this.rotate) {
                    Thread.sleep(14000);

                    while (!isCancelled()) {
                        session.sendKeepAliveMsg();

                        for (int i = 0; i < 180; i = i + 90) {
                            String commandRotate = "echo 'flytoview=<gx:duration>6</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt><longitude>" + this.currentPoi.getLongitude() +
                                    "</longitude><latitude>" + this.currentPoi.getLatitude() +
                                    "</latitude><altitude>" + this.currentPoi.getAltitude() +
                                    "</altitude><heading>" + (this.currentPoi.getHeading() + i) +
                                    "</heading><tilt>" + this.currentPoi.getTilt() +
                                    "</tilt><range>" + this.currentPoi.getRange() +
                                    "</range><gx:altitudeMode>" + this.currentPoi.getAltitudeMode() +
                                    "</gx:altitudeMode></LookAt>' > /tmp/query.txt";

                            LGUtils.setConnectionWithLiquidGalaxy(session, commandRotate, activity);
                            session.sendKeepAliveMsg();
                            Thread.sleep(6000);
                        }

                        for (int i = -180; i <= 0; i = i + 90) {
                            String commandRotate = "echo 'flytoview=<gx:duration>6</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt><longitude>" + this.currentPoi.getLongitude() +
                                    "</longitude><latitude>" + this.currentPoi.getLatitude() +
                                    "</latitude><altitude>" + this.currentPoi.getAltitude() +
                                    "</altitude><heading>" + (this.currentPoi.getHeading() + i) +
                                    "</heading><tilt>" + this.currentPoi.getTilt() +
                                    "</tilt><range>" + this.currentPoi.getRange() +
                                    "</range><gx:altitudeMode>" + this.currentPoi.getAltitudeMode() +
                                    "</gx:altitudeMode></LookAt>' > /tmp/query.txt";

                            LGUtils.setConnectionWithLiquidGalaxy(session, commandRotate, activity);
                            session.sendKeepAliveMsg();
                            Thread.sleep(6000);
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
