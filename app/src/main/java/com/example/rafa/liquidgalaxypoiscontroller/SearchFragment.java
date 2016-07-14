package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.beans.Category;
import com.example.rafa.liquidgalaxypoiscontroller.beans.POI;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.utils.LGUtils;
import com.example.rafa.liquidgalaxypoiscontroller.utils.PoisGridViewAdapter;
import com.jcraft.jsch.JSchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SearchFragment extends Fragment {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    View rootView;
    GridView poisGridView;
    private EditText editSearch;
    private FloatingActionButton buttonSearch;
    private ImageView earth, moon, mars;
    private String currentPlanet = "EARTH";
    private FloatingActionButton btnSpeak;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        editSearch = (EditText) rootView.findViewById(R.id.search_edittext);
        buttonSearch = (FloatingActionButton) rootView.findViewById(R.id.searchButton);
        earth = (ImageView) rootView.findViewById(R.id.earth);
        moon = (ImageView) rootView.findViewById(R.id.moon);
        mars = (ImageView) rootView.findViewById(R.id.mars);

        btnSpeak = (FloatingActionButton) rootView.findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        screenSizeTreatment();
        setSearchInLGButton(rootView);
        setPlanetsButtonsBehaviour();

        poisGridView = (GridView) rootView.findViewById(R.id.POISgridview);

        return rootView;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String placeToSearch = result.get(0);
                    if (!placeToSearch.equals("") && placeToSearch != null) {
                        editSearch.setText(placeToSearch);
                        String command = buildSearchCommand(placeToSearch);
                        SearchTask searchTask = new SearchTask(command, false);
                        searchTask.execute();

                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_enter_search), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentPlanet.equals("EARTH")) {
            Category category = getCategoryByName(currentPlanet);

            final List<POI> poisList = getPoisList(category.getId());
            if (poisList != null) {
                poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
            }
        }
    }

    private void setPlanetsButtonsBehaviour() {
        Earth();
        Moon();
        Mars();
    }

    private void Earth() {
        earth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = "echo 'planet=earth' > /tmp/query.txt";

                if (!currentPlanet.equals("EARTH")) {
                    SearchTask searchTask = new SearchTask(command, true);
                    searchTask.execute();
                    currentPlanet = "EARTH";
                    Category category = getCategoryByName(currentPlanet);

                    final List<POI> poisList = getPoisList(category.getId());
                    if (poisList != null) {
                        poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
                    }
                }
            }
        });
    }


    private List<POI> getPoisList(int categoryId) {
        //TODO: Get pois of subcategories that have father_id=categoryId
        List<POI> lPois = new ArrayList<>();

        Cursor allPoisByCategoryCursor = POIsContract.POIEntry.getPOIsByCategory(getActivity(), String.valueOf(categoryId));

        while (allPoisByCategoryCursor.moveToNext()) {

            int poiId = allPoisByCategoryCursor.getInt(0);

            POI poiEntry = getPoiData(poiId);
            lPois.add(poiEntry);
        }

        return lPois;
    }

    private POI getPoiData(int poiId) {
        POI poiEntry = new POI();
        Cursor poiCursor = POIsContract.POIEntry.getPoiByID(poiId);

        if (poiCursor.moveToNext()) {

            poiEntry.setId(poiCursor.getLong(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_ID)));
            poiEntry.setName(poiCursor.getString(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_COMPLETE_NAME)));
            poiEntry.setAltitude(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_ALTITUDE)));
            poiEntry.setAltitudeMode(poiCursor.getString(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE)));
            poiEntry.setCategoryId(poiCursor.getInt(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_CATEGORY_ID)));
            poiEntry.setHeading(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_HEADING)));
            poiEntry.setLatitude(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_LATITUDE)));
            poiEntry.setLongitude(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_LONGITUDE)));
            poiEntry.setHidden(poiCursor.getInt(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_HIDE)) == 1);
            poiEntry.setRange(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_RANGE)));
            poiEntry.setTilt(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_TILT)));
            poiEntry.setVisited_place(poiCursor.getString(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME)));
        }

        poiCursor.close();
        return poiEntry;
    }

    private Category getCategoryByName(String categoryName) {
        Category category = new Category();
        Cursor categoryCursor = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), categoryName);

        if (categoryCursor.moveToNext()) {
            category.setId(categoryCursor.getInt(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_ID)));
            category.setFatherID(categoryCursor.getInt(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_FATHER_ID)));
            category.setName(categoryCursor.getString(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_NAME)));
            category.setShownName(categoryCursor.getString(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME)));
        }

        return category;
    }

    private void Moon() {

        moon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = "echo 'planet=moon' > /tmp/query.txt";
                if (!currentPlanet.equals("MOON")) {
                    //setConnectionWithLiquidGalaxy(command);
                    SearchTask searchTask = new SearchTask(command, true);
                    searchTask.execute();
                    currentPlanet = "MOON";
                    Category category = getCategoryByName(currentPlanet);
                    final List<POI> poisList = getPoisList(category.getId());
                    poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
                }
            }
        });
    }

    private void Mars() {

        mars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = "echo 'planet=mars' > /tmp/query.txt";
                if (!currentPlanet.equals("MARS")) {
                    SearchTask searchTask = new SearchTask(command, true);
                    searchTask.execute();
                    currentPlanet = "MARS";
                    Category category = getCategoryByName(currentPlanet);
                    final List<POI> poisList = getPoisList(category.getId());
                    poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
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


        } else if (smallestWidth <= 720 && smallestWidth >= 600) {
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

                    String command = buildSearchCommand(placeToSearch);
                    SearchTask searchTask = new SearchTask(command, false);
                    searchTask.execute();

                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.please_enter_search), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String buildSearchCommand(String search) {
        return "echo 'search=" + search + "' > /tmp/query.txt";
    }


    private class SearchTask extends AsyncTask<Void, Void, String> {

        String command;
        boolean isChangingPlanet;
        private ProgressDialog dialog;

        public SearchTask(String command, boolean isChangingPlanet) {
            this.command = command;
            this.isChangingPlanet = isChangingPlanet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(getActivity());
                if (isChangingPlanet) {
                    dialog.setMessage(getResources().getString(R.string.changingPlanet));
                } else {
                    dialog.setMessage(getResources().getString(R.string.searching));
                }
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
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
                return LGUtils.setConnectionWithLiquidGalaxy(command, getActivity());
            } catch (JSchException e) {
                cancel(true);
                Toast.makeText(getActivity(), getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
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

}