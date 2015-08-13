package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;


public class MainFragment extends Fragment {

    private final String correctPassword = "lg";
    private PopupWindow popupWindow;
    private Button stopTour;

    public MainFragment() {
    }

    /**
     * When the view is created, set the behaviour of the POIs and TOURs button
     * when they are clicked and also the behaviour of the administration tools button.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        stopTour = (Button) rootView.findViewById(R.id.stop_tour);

        setPOIButtonBehaviour(rootView);
        setTourButtonBehaviour(rootView);
        setAdminButtonBehaviour(rootView, R.id.admin_button);
        setSearchInLGButton(rootView);

        return rootView;
    }

    private void setSearchInLGButton(View rootView) {

        final EditText editSearch = (EditText) rootView.findViewById(R.id.search_edittext);
        Button buttonSearch = (Button) rootView.findViewById(R.id.searchButton);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String placeToSearch = editSearch.getText().toString();
            if(!placeToSearch.equals("") && placeToSearch != null){
                try {
                    String command = buildSearchCommand(placeToSearch);
                    setConnectionWithLiquidGalaxy(command);
                }catch(JSchException ex){
                    Toast.makeText(getActivity(), "Error connecting with Liquid Galaxy. Try changing username, password, host IP or port.", Toast.LENGTH_LONG).show();
                }
            }else{
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
    /**
     * Initializes an instance of POISFragment to show the content of the POIs or TOURs database,
     * depending on which button has been clicked.
     * @param rootView Where we place all elements (buttons, POIs or TOURs fragment...)
     */
    private void setPOIButtonBehaviour(View rootView){

        final Button button = (Button) rootView.findViewById(R.id.POI_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.POIS_container, new POISFragment(), "USER/POIS").commit();
                showToastMessage("POI");
            }
        });
    }

    private void setTourButtonBehaviour(View rootView){

        final Button button = (Button) rootView.findViewById(R.id.TOUR_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.POIS_container, new POISFragment(), "USER/TOURS").commit();
                showToastMessage("TOUR");
            }
        });
    }

    /**
     * When Administration Tools button is clicked, starts the interaction with the popup that ask
     * to access to that tools.
     * @param rootView Where we place all elements (buttons, POIs or TOURs fragment...)
     * @param adminButtonID Identifier of administration button
     */
    private void setAdminButtonBehaviour(View rootView, final int adminButtonID){

        final Button button = (Button) rootView.findViewById(adminButtonID);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupInteraction();
            }
        });
    }

    /**
     * Gets the popup view by creating it and sets done and cancel buttons treatment
     */
    private void popupInteraction(){

        View popupView = createAndShowPopup();
        doneButtonTreatment(popupView);
        cancelButtonTreatment(popupView);
    }

    /**
     * Creates the popup view to be used for asking the user to enter in administration section.
     * @return The popup view created.
     */
    private View createAndShowPopup(){
        final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_admin_password, null);

        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        return popupView;
    }
    private void cancelButtonTreatment(View popupView){
        final Button cancelButton = (Button) popupView.findViewById(R.id.pass_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});
    }
    private void doneButtonTreatment(final View popupView){

        final Button doneButton = (Button) popupView.findViewById(R.id.pass_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView incorrectPass = (TextView) popupView.findViewById(R.id.incorrect_pass);
                String password = getPasswordFromUser(popupView);
                if(password.equals(correctPassword)){
                    if(incorrectPass.isShown())
                    {
                        incorrectPass.setVisibility(View.GONE);
                    }
                    Intent intent = new Intent(getActivity(), AdminActivity.class);//diria que es getActivity()
                    startActivity(intent);
                    popupWindow.dismiss();
                }
                else{
                    incorrectPass.setVisibility(View.VISIBLE);
                    showToastMessage("INCORRECT PASSWORD");
                }
            }
        });

    }
    private String getPasswordFromUser(View popupView){
        EditText editText = (EditText) popupView.findViewById(R.id.password);
        return editText.getText().toString();
    }

    private void showToastMessage(String text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
