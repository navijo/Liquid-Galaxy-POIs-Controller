package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;


public class SearchFragment extends Fragment {

    private EditText editSearch;
    private FloatingActionButton buttonSearch;
    private ImageView earth, moon, mars;
    private String currentPlanet = "EARTH";

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        editSearch = (EditText) rootView.findViewById(R.id.search_edittext);
        buttonSearch = (FloatingActionButton) rootView.findViewById(R.id.searchButton);
        earth = (ImageView) rootView.findViewById(R.id.earth);
        moon = (ImageView) rootView.findViewById(R.id.moon);
        mars = (ImageView) rootView.findViewById(R.id.mars);
        screenSizeTreatment();
        setSearchInLGButton(rootView);
        setPlanetsButtonsBehaviour();

        return rootView;
    }

    private void setPlanetsButtonsBehaviour(){
        Earth();
        Moon();
        Mars();
    }

    private void Earth(){
        earth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = "echo 'planet=earth' > /tmp/query.txt";
                try {
                    if(!currentPlanet.equals("EARTH")) {
                        setConnectionWithLiquidGalaxy(command);
                        currentPlanet = "EARTH";
                    }
                } catch (JSchException e) {
                    Toast.makeText(getActivity(), "Error changing planet, try changing connection settings", Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void Moon(){

        moon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = "echo 'planet=moon' > /tmp/query.txt";
                try {
                    if(!currentPlanet.equals("MOON")) {
                        setConnectionWithLiquidGalaxy(command);
                        currentPlanet = "MOON";
                    }
                } catch (JSchException e) {
                    Toast.makeText(getActivity(), "Error changing planet, try changing connection settings", Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void Mars(){

        mars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = "echo 'planet=mars' > /tmp/query.txt";
                try {
                    if(!currentPlanet.equals("MARS")) {
                        setConnectionWithLiquidGalaxy(command);
                        currentPlanet = "MARS";
                    }
                } catch (JSchException e) {
                    Toast.makeText(getActivity(), "Error changing planet, try changing connection settings", Toast.LENGTH_LONG);
                }
            }
        });
    }


    private void screenSizeTreatment() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;


        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth > 720) {
            editSearch.setTextSize(50);
            earth.getLayoutParams().height = 160;
            moon.getLayoutParams().height = 160;
            mars.getLayoutParams().height = 160;
            earth.getLayoutParams().width = 160;
            moon.getLayoutParams().width = 160;
            mars.getLayoutParams().width = 160;
            earth.requestLayout();
            moon.requestLayout();
            mars.requestLayout();


        } else if(smallestWidth <= 720 && smallestWidth >= 600 ){
            editSearch.setTextSize(40);
            earth.getLayoutParams().height = 120;
            moon.getLayoutParams().height = 120;
            mars.getLayoutParams().height = 120;
            earth.getLayoutParams().width = 120;
            moon.getLayoutParams().width = 120;
            mars.getLayoutParams().width = 120;
            earth.requestLayout();
            moon.requestLayout();
            mars.requestLayout();
        } else {
            earth.getLayoutParams().height = 75;
            moon.getLayoutParams().height = 75;
            mars.getLayoutParams().height = 75;
            earth.getLayoutParams().width = 75;
            moon.getLayoutParams().width = 75;
            mars.getLayoutParams().width = 75;
            earth.requestLayout();
            moon.requestLayout();
            mars.requestLayout();
        }

    }

    private void setSearchInLGButton(View rootView) {


        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String placeToSearch = editSearch.getText().toString();
                if (!placeToSearch.equals("") && placeToSearch != null) {
                    try {
                        String command = buildSearchCommand(placeToSearch);
                        setConnectionWithLiquidGalaxy(command);
                    } catch (JSchException ex) {
                        Toast.makeText(getActivity(), "Error connecting with Liquid Galaxy. Try changing username, password, host IP or port.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please, first type some place to search.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private String buildSearchCommand(String search){
        return "echo 'search=" + search + "' > /tmp/query.txt";
    }
    private String setConnectionWithLiquidGalaxy(String command) throws JSchException {

        JSch jsch = new JSch();

        Session session = jsch.getSession("lg", "172.26.17.21", 22);
        session.setPassword("lqgalaxy");

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