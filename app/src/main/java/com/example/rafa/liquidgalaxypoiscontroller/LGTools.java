package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class LGTools extends Fragment {

    private String filePath = "";
    private Button importPois, relaunch, reboot, shutDown;

    public LGTools() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lgtools, container, false);
        importPois = (Button) view.findViewById(R.id.import_pois);
        relaunch = (Button) view.findViewById(R.id.relaunch);
        reboot = (Button) view.findViewById(R.id.reboot);
        shutDown = (Button) view.findViewById(R.id.shutdown);
        setImportPOIsButtonBehaviour();
        setRelaunchButtonBehaviour();
        setRebootButtonBehaviour();
        setShutDownButtonBehaviour();
        return view;
    }

    /*SHUT DOWN*/
    private void setShutDownButtonBehaviour() {
        shutDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String sentence = "/home/lg/bin/lg-sudo 'shutdown -h 0' > /home/lg/log.txt";
                    showAlertAndExecution(sentence, "shut down");
                }catch (Exception e){
                    Toast.makeText(getActivity(),"Error with Liquid Galaxy.",Toast.LENGTH_LONG);
                }
            }
        });
    }

    /*REBOOT*/
    private void setRebootButtonBehaviour() {
        reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String sentence = "/home/lg/bin/lg-sudo reboot > /home/lg/log.txt";
                    showAlertAndExecution(sentence, "reboot");
                }catch (Exception e){
                    Toast.makeText(getActivity(),"Error with Liquid Galaxy.",Toast.LENGTH_LONG);
                }
            }
        });
    }

    /*RELAUNCH*/
    private void setRelaunchButtonBehaviour() {
        relaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String sentence = "/home/lg/bin/lg-relaunch > /home/lg/log.txt";
                    showAlertAndExecution(sentence, "relaunch");
                }catch (Exception e){
                    Toast.makeText(getActivity(),"Error with Liquid Galaxy.",Toast.LENGTH_LONG);
                }
            }
        });
    }

    /*SHUT DOWN, RELAUNCH and REBOOT*/
    private void showAlertAndExecution(final String sentence, String action){
        // prepare the alert box
        AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());

        // set the message to display
        alertbox.setMessage("Are you sure to " + action + " Liquid Galaxy?");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    setConnectionWithLiquidGalaxy(sentence);
                } catch (JSchException e) {
                    Toast.makeText(getActivity(),"Error connecting with Liquid Galaxy. Please, try changing settings.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }
    private void setConnectionWithLiquidGalaxy(String command) throws JSchException {

        //We get the mandatory settings to be able to connect with Liquid Galaxy system.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String user = prefs.getString("User", "lg");
        String password = prefs.getString("Password", "lqgalaxy");
        String hostname = prefs.getString("HostName", "172.26.17.21");
        int port = Integer.parseInt(prefs.getString("Port", "22"));

        Toast.makeText(getActivity(), user + password + hostname + port, Toast.LENGTH_LONG).show();

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
        Toast.makeText(getActivity(),baos.toString(),Toast.LENGTH_LONG).show();
        channelssh.disconnect();

    }

    /*IMPORT POIS*/
    private void setImportPOIsButtonBehaviour() {
        importPois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Complete procedure to get the file with the POIs to import
                selectFileToImport();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //When user select one file
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                Uri uri = data.getData();
                if (uri != null) {
                    String path = uri.toString();
                    //We get the file path by executing one of the following methods, depending the explorer the user uses.
                    if (path.toLowerCase().startsWith("file://")) {
                        filePath = (new File(URI.create(path))).getAbsolutePath();
                    }else {
                        filePath = pathTreatment(data.getData().getPath(), Environment.getExternalStorageDirectory().getAbsolutePath());
                    }
                    //We get the file name in order to create the category which will contain the POIs.
                    String category = getFileName();
                    int categoryID = createCategory(category);
                    if(categoryID != 0){
                        //We read the file and create the POIs described inside it.
                        List<ContentValues> poisToImport = readFile(categoryID);
                        createPOis(poisToImport);
                    }else{
                        Toast.makeText(getActivity(), "There is an error creating the category. Try to solve it renaming the file.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
    private List<ContentValues> readFile(int categoryID) {
        List<ContentValues> poisList = new ArrayList<ContentValues>();
        File file = new File(filePath);
        if(file.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

            try {
                while ((line = br.readLine()) != null) {
                    //for each POI described inside the file we read and introduce it inside a POIs list.
                    if(!line.equals("") && line != null) {
                        readPOI(poisList, line, categoryID);
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getActivity(), "File couldn't be opened. Try to open it with a different file explorer, for example 'Root Explorer' once.", Toast.LENGTH_LONG).show();
        }
        return poisList;
    }
    private void readPOI(List<ContentValues> poisList, String line, int categoryID){

        try {
            ContentValues poi = new ContentValues();
            String name = getPOIName(line);
            String visited_place = name;
            String longitude = getPOIAttribute("longitude", line);
            String latitude = getPOIAttribute("latitude", line);
            String altitude = getPOIAttribute("altitude", line);
            String heading = getPOIAttribute("heading", line);
            String tilt = getPOIAttribute("tilt", line);
            String range = getPOIAttribute("range", line);
            String altitudeMode = getAltitudeMode(line);

            poi.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, name);
            poi.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, visited_place);
            poi.put(POIsContract.POIEntry.COLUMN_LONGITUDE, longitude);
            poi.put(POIsContract.POIEntry.COLUMN_LATITUDE, latitude);
            poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE, altitude);
            poi.put(POIsContract.POIEntry.COLUMN_HEADING, heading);
            poi.put(POIsContract.POIEntry.COLUMN_TILT, tilt);
            poi.put(POIsContract.POIEntry.COLUMN_RANGE, range);
            poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, altitudeMode);
            poi.put(POIsContract.POIEntry.COLUMN_HIDE, 0);
            poi.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, categoryID);

            poisList.add(poi);
        }catch (Exception e){
            Toast.makeText(getActivity(),"Error reading POIs. Remember POI name must be between two '@' and other features between '<featureName>' fields.", Toast.LENGTH_LONG).show();
        }
    }
    private String getFileName(){
        int startIndex = filePath.lastIndexOf("/") + 1;
        return filePath.substring(startIndex, filePath.length() - 4);
    }
    private void selectFileToImport(){
        //We use Intent.GATA_GET_CONTENT to let the user select the file to import
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if ( intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }
    private String getPOIName(String line){
        int start = line.indexOf("@") + 1;
        int end = line.lastIndexOf("@");
        return line.substring(start, end);
    }
    private String getPOIAttribute(String attribute, String line){

        int attributeSize = attribute.length();
        int start = line.indexOf("<" + attribute + ">") + 1 + attributeSize + 1;
        int end = line.lastIndexOf("</" + attribute + ">");
        return line.substring(start, end);

    }
    private String getAltitudeMode(String line){

        int attributeSize = "gx:altitudeMode>".length();
        int start = line.indexOf("<gx:altitudeMode>") + attributeSize + 1;
        int end = line.indexOf("</gx:altitudeMode>");
        return line.substring(start, end);
    }
    private void createPOis(List<ContentValues> pois){
        for(ContentValues poi: pois){
            try {
                Uri insertedUri = POIsContract.POIEntry.createNewPOI(getActivity(), poi);
                Toast.makeText(getActivity(), insertedUri.toString(), Toast.LENGTH_SHORT).show();
            }catch (android.database.SQLException e){
                String poiName = poi.get(POIsContract.POIEntry.COLUMN_COMPLETE_NAME).toString();
                Toast.makeText(getActivity(), "Already exists one POI with the same name: " + poiName + ". Please, change it.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private int createCategory(String categoryName){

        int categoryID = 0;
        ContentValues category = new ContentValues();
        category.put(POIsContract.CategoryEntry.COLUMN_NAME, categoryName);
        category.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, 0);
        category.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, categoryName + "/");
        category.put(POIsContract.CategoryEntry.COLUMN_HIDE, 0);

        try {
            Uri insertedUri = POIsContract.CategoryEntry.createNewCategory(getActivity(), category);
            categoryID = POIsContract.CategoryEntry.getIdByUri(insertedUri);
            Toast.makeText(getActivity(), "Category: " + insertedUri.toString(), Toast.LENGTH_LONG).show();
        }catch (android.database.SQLException e){
            Toast.makeText(getActivity(), "Already exists one category with the same name. Please, change it.", Toast.LENGTH_SHORT).show();
        }
        return categoryID;
    }
    private String pathTreatment(String path, String absolutePath){
        int start = 0;
        String firstPathFolder = "";
        if(path.contains(":")) {
            start = path.indexOf(":") + 1;
            path = path.substring(start);
        }

        if(path.startsWith("/")){
            firstPathFolder = path.split("/")[1];
        }else {
            firstPathFolder = path.split("/")[0];
        }
        String[] absoluteFolders = absolutePath.split("/");
        String lastAbsoluteFolder = absoluteFolders[absoluteFolders.length-1];

        if(firstPathFolder.equals(lastAbsoluteFolder)){
            if(path.startsWith("/")){
                path = path.substring(firstPathFolder.length() + 2);
            }else{
                path = path.substring(firstPathFolder.length() + 1);
            }
        }

        if(!absolutePath.endsWith("/")){
            absolutePath = absolutePath + "/";
        }
        return absolutePath + path;
    }
}
