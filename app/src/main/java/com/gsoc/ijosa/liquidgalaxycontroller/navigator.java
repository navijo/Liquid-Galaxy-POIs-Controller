package com.gsoc.ijosa.liquidgalaxycontroller;

/**
 * Created by syedaliahmed on 11/01/18.
 */
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.gsoc.ijosa.liquidgalaxycontroller.beans.POI;

public class navigator extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, TextWatcher {

    private Button upButton, downButton, leftButton, rightButton, zoomIn, zoomOut;

    public navigator

    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.navigator_activity, container, false);
        initialization(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Controller");
    }

    private void initialization(View rootView) {
        upButton = (Button) rootView.findViewById(R.id.upButton);
        downButton = (Button) rootView.findViewById(R.id.downButton);
        leftButton = (Button) rootView.findViewById(R.id.leftButton);
        rightButton = (Button) rootView.findViewById(R.id.rightButton);

        zoomIn = (Button) rootView.findViewById(R.id.zoomIn);
        zoomOut = (Button) rootView.findViewById(R.id.zoomOut);

        upButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
    }

    private class VisitPoiTask extends AsyncTask<Void, Void, String> {

        int id = v.getId();
        int keyCode = 17; // dummy initialisation
        String action = "TYPE_KEY";
        switch(id)

        {
            case R.id.upButton:
                keyCode = 38;
                break;
            case R.id.downButton:
                keyCode = 40;
                break;
            case R.id.leftButton:
                keyCode = 37;
                break;
            case R.id.rightButton:
                keyCode = 39;
                break;
            case R.id.zoomIn:
                keyCode = 107;
                break;
            case R.id.zoomOut:
                keyCode = 109;
                break;


        }

        VisitPoiTask(int id, int keyCode) {
            this.id = id;
            this.keyCode;
        }
    }
}