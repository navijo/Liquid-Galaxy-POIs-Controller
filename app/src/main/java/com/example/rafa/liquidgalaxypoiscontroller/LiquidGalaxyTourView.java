package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.POIEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.TourPOIsEntry;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class LiquidGalaxyTourView extends AsyncTask<String, Void, String> {
    private static final String TAG;
    private FragmentActivity poisFragmentAct;

    /* renamed from: com.example.rafa.liquidgalaxypoiscontroller.LiquidGalaxyTourView.1 */
    class TourDialog implements OnClickListener {
        TourDialog() {
        }

        public void onClick(DialogInterface arg0, int arg1) {
        }
    }

    static {
        TAG = LiquidGalaxyTourView.class.getSimpleName();
    }

    protected String doInBackground(String... params) {
        List<HashMap<String, String>> pois = new ArrayList();
        List<Integer> poisDuration = new ArrayList();
        if (params.length == 0 || params == null) {
            return "Error. There's no item selected.";
        }
        try {
            Cursor c = TourPOIsEntry.getPOIsByTourID(this.poisFragmentAct, params[0]);
            while (c.moveToNext()) {
                int poiID = c.getInt(0);
                poisDuration.add(Integer.valueOf(c.getInt(2)));
                pois.add(getPOIData(poiID));
            }
            try {
                sendTourPOIs(pois, poisDuration);
                return BuildConfig.FLAVOR;
            } catch (IndexOutOfBoundsException e) {
                return "Error. There's probably no POI inside the Tour.";
            }
        } catch (Exception e2) {
            return "Error. Tour POIs cannot be read.";
        }
    }

    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        cancel(true);
        POISFragment.resetTourSettings();
        if (s.startsWith("Error")) {
            Builder alertbox = new Builder(this.poisFragmentAct);
            alertbox.setTitle("Error");
            alertbox.setMessage("There's probably no POI inside this Tour");
            alertbox.setPositiveButton("OK", new TourDialog());
            alertbox.show();
        }
    }

    public void setActivity(FragmentActivity activity) {
        this.poisFragmentAct = activity;
    }

    private HashMap<String, String> getPOIData(int id) throws Exception {
        Cursor c = POIEntry.getPOIByID(this.poisFragmentAct, String.valueOf(id));
        HashMap<String, String> poi = new HashMap();
        if (c.moveToNext()) {
            poi.put("completeName", c.getString(1));
            poi.put("longitude", String.valueOf(c.getFloat(3)));
            poi.put("latitude", String.valueOf(c.getFloat(4)));
            poi.put("altitude", String.valueOf(c.getFloat(5)));
            poi.put("heading", String.valueOf(c.getFloat(6)));
            poi.put("tilt", String.valueOf(c.getFloat(7)));
            poi.put("range", String.valueOf(c.getFloat(8)));
            poi.put("altitudeMode", c.getString(9));
            return poi;
        }
        throw new Exception("There is no POI with this features inside the data base. Try creating once correct.");
    }

    private String buildCommand(HashMap<String, String> poi) {
        return "echo 'flytoview=<LookAt><longitude>" + ((String) poi.get("longitude")) + "</longitude><latitude>" + ((String) poi.get("latitude")) + "</latitude><altitude>" + ((String) poi.get("altitude")) + "</altitude><heading>" + ((String) poi.get("heading")) + "</heading><tilt>" + ((String) poi.get("tilt")) + "</tilt><range>" + ((String) poi.get("range")) + "</range><gx:altitudeMode>" + ((String) poi.get("altitudeMode")) + "</gx:altitudeMode></LookAt>' > /tmp/query.txt";
    }

    private void sendTourPOIs(List<HashMap<String, String>> pois, List<Integer> poisDuration) {
        sendFirstTourPOI((HashMap) pois.get(0));
        sendOtherTourPOIs(pois, poisDuration);
    }

    private void sendOtherTourPOIs(List<HashMap<String, String>> pois, List<Integer> poisDuration) {
        int i = 1;
        while (!isCancelled()) {
            sendTourPOI(Integer.valueOf(((Integer) poisDuration.get(i)).intValue()), buildCommand((HashMap) pois.get(i)));
            i++;
            if (i == pois.size()) {
                i = 0;
            }
        }
    }

    private void sendFirstTourPOI(HashMap<String, String> firstPoi) {
        try {
            setConnectionWithLiquidGalaxy(buildCommand(firstPoi));
            Log.d(TAG, "First send");
        } catch (JSchException e) {
            Log.d(TAG, "Error in connection with Liquid Galaxy.");
        }
    }

    private void sendTourPOI(Integer duration, String command) {
        try {
            Thread.sleep((long) (duration.intValue() * 1000));
            setConnectionWithLiquidGalaxy(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in duration of POIs.");
        } catch (JSchException e2) {
            Log.d(TAG, "Error connecting with LG.");
        }
    }

    protected void onCancelled() {
        super.onCancelled();
    }

    private String setConnectionWithLiquidGalaxy(String command) throws JSchException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.poisFragmentAct);
        String user = prefs.getString("User", "lg");
        String password = prefs.getString("Password", "lqgalaxy");
        Session session = new JSch().getSession(user, prefs.getString("HostName", "172.26.17.21"), Integer.parseInt(prefs.getString("Port", "22")));
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
