package com.example.rafa.liquidgalaxypoiscontroller.data;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by RAFA on 09/08/2015.
 */
public class LiquidGalaxyTourView extends AsyncTask<String, Void, String>{

    private FragmentActivity poisFragmentAct;
    private String tourStatus = "Avaliable";
    private static final String TAG = LiquidGalaxyTourView.class.getSimpleName();
    private int TRAVEL_TIME = 10*1000;


    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(String... params) {
        final List<HashMap<String, String>> pois = new ArrayList<HashMap<String, String>>();
        final List<Integer> poisDuration = new ArrayList<Integer>();

        if(params.length == 0 || params == null){
            return "ERROR";
        }

        try {
            Cursor c = POIsContract.TourPOIsEntry.getPOIsByTourID(poisFragmentAct, params[0]);
            int poiID = 0;
            while(c.moveToNext()){
                poiID = c.getInt(0);
                poisDuration.add(c.getInt(2));
                HashMap<String, String> poi = getPOIData(poiID);
                pois.add(poi);
            }
        }catch (Exception ex){
            Log.d(TAG, "First send" + ex.getMessage().toString());
        }
        sendTourPOIs(pois, poisDuration);

        return "";
    }

    public void setActivity(FragmentActivity activity){
        poisFragmentAct = activity;
    }
    private HashMap<String, String> getPOIData(int id) throws Exception {
        Cursor c = POIsContract.POIEntry.getPOIByID(poisFragmentAct, String.valueOf(id));
        HashMap<String, String> poi = new HashMap<String, String>();

        if(c.moveToNext()) {
            poi.put("completeName", c.getString(1));
            poi.put("longitude", String.valueOf(c.getFloat(3)));
            poi.put("latitude", String.valueOf(c.getFloat(4)));
            poi.put("altitude", String.valueOf(c.getFloat(5)));
            poi.put("heading", String.valueOf(c.getFloat(6)));
            poi.put("tilt", String.valueOf(c.getFloat(7)));
            poi.put("range", String.valueOf(c.getFloat(8)));
            poi.put("altitudeMode", c.getString(9));
        }else{
            throw new Exception("There is no POI with this features inside the data base. Try creating once correct.");
        }
        return poi;
    }
    private String buildCommand(HashMap<String, String> poi){
        return "echo 'flytoview=<LookAt><longitude>" + poi.get("longitude") +
                "</longitude><latitude>" + poi.get("latitude") +
                "</latitude><altitude>" + poi.get("altitude") +
                "</altitude><heading>" + poi.get("heading") +
                "</heading><tilt>" + poi.get("tilt") +
                "</tilt><range>" + poi.get("range") +
                "</range><gx:altitudeMode>" + poi.get("altitudeMode") +
                "</gx:altitudeMode></LookAt>' > /tmp/query.txt";
    }
    private void sendTourPOIs(List<HashMap<String, String>> pois, List<Integer> poisDuration) {

        sendFirstTourPOI(pois.get(0));
        sendOtherTourPOIs(pois, poisDuration);
    }
    private void sendOtherTourPOIs(List<HashMap<String, String>> pois, List<Integer> poisDuration){

        String command;
        int duration = 0, i = 1;

        while(!isCancelled()){
            command = buildCommand(pois.get(i));
            duration = poisDuration.get(i);
            sendTourPOI(duration, command);
            i++;
            if(i == pois.size()){
                i = 0;
            }
        }

        if(tourStatus.equals("Error")){
            Log.d(TAG, "Error connectiong with LG. Change settings, please.");
        }
    }
    private void sendFirstTourPOI(HashMap<String, String> firstPoi) {
        String command = buildCommand(firstPoi);
        try {
            setConnectionWithLiquidGalaxy(command);
            Log.d(TAG, "First send");
        } catch (JSchException e) {
            Log.d(TAG, "Error in connection with Liquid Galaxy.");
        }
    }

    private void sendTourPOI(final Integer duration, final String command) {

        try {
            Thread.sleep(duration*1000 + TRAVEL_TIME);
            setConnectionWithLiquidGalaxy(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in duration of POIs.");
        } catch (JSchException ex){
            Log.d(TAG, "Error connecting with LG.");
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    private String setConnectionWithLiquidGalaxy(String command) throws JSchException {

        //We get the mandatory settings to be able to connect with Liquid Galaxy system.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(poisFragmentAct);
        String user = prefs.getString("User", "lg");
        String password = prefs.getString("Password", "lqgalaxy");
        String hostname = prefs.getString("HostName", "172.26.17.21");
        int port = Integer.parseInt(prefs.getString("Port", "22"));

        JSch jsch = new JSch();

        Session session = jsch.getSession(user, hostname, port);
        session.setPassword(password);

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();

        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        channelssh.setCommand(command);
        channelssh.connect();
        channelssh.disconnect();

        return baos.toString();
    }
}
