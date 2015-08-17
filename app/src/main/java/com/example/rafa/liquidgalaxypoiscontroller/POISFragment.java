package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.data.LiquidGalaxyTourView;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class POISFragment extends Fragment {

    public static LiquidGalaxyTourView tour;
    private CategoriesAdapter adapter;
    private POIsAdapter adapterPOI;
    private ListView poisListView, categoriesListView;
    private PopupWindow popupPoiSelected;
    private View popupView, poisView, dialogView;
    private Dialog dialog;
    private final String POI_IDselection = POIsContract.POIEntry._ID + " = ?";
    private final String TOUR_IDselection = POIsContract.TourEntry._ID + " = ?";
    private final String Category_IDselection = POIsContract.CategoryEntry._ID + " = ?";
    private final Uri POI_URI = POIsContract.POIEntry.CONTENT_URI;
    private final Uri TOUR_URI = POIsContract.TourEntry.CONTENT_URI;
    private final Uri Category_URI = POIsContract.CategoryEntry.CONTENT_URI;
    private static FragmentActivity currentActivity;
    private String EDITABLE_TAG, notify, createORupdate = "";
    private ImageView backIcon, backStartIcon;
    private List<String> backIDs = new ArrayList<String>(){{
            add("0");
    }};
    private TextView seeingOptions, poisListViewTittle, route, categories_tittle;
    public static int routeID = 0;
    private LinearLayout additionLayout, poisfragment;
    private FloatingActionButton createPOI, createPOIhere, createTour, createCategory, createTourhere,createCategoryhere,cancel,edit,delete;
    private static FloatingActionButton stopButton;

    private static boolean tourIsWorking = false;

    public POISFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getArguments() !=null) {
            createORupdate = getArguments().getString("createORupdate");
            EDITABLE_TAG = getArguments().getString("EDITABLE");
        }

        poisView = inflater.inflate(R.layout.fragment_pois, container, false);
        currentActivity = getActivity();

        ListTreatment();

        return poisView;
    }

    private void ListTreatment(){

        //poisListView = (DynamicListView) poisView.findViewById(R.id.pois_listview);
        poisListView = (ListView) poisView.findViewById(R.id.pois_listview);
        categoriesListView = (ListView) poisView.findViewById(R.id.categories_listview);
        backIcon = (ImageView) poisView.findViewById(R.id.back_icon);
        backStartIcon = (ImageView) poisView.findViewById(R.id.back_start_icon);
        seeingOptions = (TextView) poisView.findViewById(R.id.see_all_or_by_category);
        poisListViewTittle = (TextView) poisView.findViewById(R.id.pois_tittle_listview);
        route = (TextView) poisView.findViewById(R.id.fragment_pois_route);
        poisfragment = (LinearLayout) poisView.findViewById(R.id.pois_xml_fragment);
        stopButton = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.tour_stop);
        categories_tittle = (TextView) poisView.findViewById(R.id.categories_textview);

        additionLayout = (LinearLayout) poisView.findViewById(R.id.addition_buttons_layout);
        createPOI = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_poi);
        createCategory = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_category);
        createTour = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_tour);
        createPOIhere = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_poi_here);
        createCategoryhere = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_category_here);
        createTourhere = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_tour_here);

        dialogView = getLayoutInflater(getArguments()).inflate(R.layout.dialog_item_options, null);
        dialog = getDialogByView(dialogView);
        cancel = (FloatingActionButton) dialogView.findViewById(R.id.cancel_poi_selection);
        edit = (FloatingActionButton) dialogView.findViewById(R.id.edit_poi);
        delete = (FloatingActionButton) dialogView.findViewById(R.id.delete_poi);

//        if(tour != null) {
//            tour.cancel(true);
//        }
//        tour = new LiquidGalaxyTourView();

        screenSizeTreatment();
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

        if (smallestWidth >= 1000) {
            seeingOptions.setTextSize(28);
            categories_tittle.setTextSize(28);
            route.setTextSize(28);
            poisListViewTittle.setTextSize(28);
            backStartIcon.setImageResource(R.drawable.ic_home_black_48dp);
            backIcon.setImageResource(R.drawable.ic_reply_black_48dp);
        } else if(smallestWidth >720 && smallestWidth<1000){
            seeingOptions.setTextSize(24);
            categories_tittle.setTextSize(24);
            route.setTextSize(24);
            poisListViewTittle.setTextSize(24);
            backStartIcon.setImageResource(R.drawable.ic_home_black_36dp);
            backIcon.setImageResource(R.drawable.ic_reply_black_36dp);
        }
        else if(smallestWidth <= 720 && smallestWidth >= 600 ){
            seeingOptions.setTextSize(22);
            categories_tittle.setTextSize(2);
            route.setTextSize(2);
            poisListViewTittle.setTextSize(22);
        } else if(smallestWidth < 600 && smallestWidth >= 500 ){
            seeingOptions.setTextSize(17);
            categories_tittle.setTextSize(17);
            route.setTextSize(17);
            poisListViewTittle.setTextSize(17);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setNewItemHereButtonBehaviour();
        setNewItemButtonBehaviour();//Creation of a new item

        if(EDITABLE_TAG.endsWith("POIS")){
            showPOIs();
        }
        if(EDITABLE_TAG.endsWith("TOURS")){
            showTOURs();
        }
        if(EDITABLE_TAG.equals("ADMIN/CATEGORIES")){
            createPOI.setVisibility(View.GONE);
            showCategories();
        }
    }

    /*CATEGORIES TREATMENT */
    private void showCategories(){

        additionLayout.setVisibility(View.VISIBLE);
        createCategory.setVisibility(View.VISIBLE);
        createCategoryhere.setVisibility(View.GONE);
        poisListView.setVisibility(View.GONE);
        showAllCategories();

        seeingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seeingOptions.getText().toString().equals("See elements by category")){
                    seeingOptions.setText("See all elements");
                    createCategoryhere.setVisibility(View.VISIBLE);
                    notify = "CATEGORY";
                    showCategoriesByLevel();

                }else{
                    seeingOptions.setText("See elements by category");
                    backIDs.clear();
                    backIDs.add("0");
                    createCategoryhere.setVisibility(View.GONE);
                    showAllCategories();
                }
            }
        });
    }
    private int showCategoriesByLevel() {

        Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")){
            queryCursor = POIsContract.CategoryEntry.getNotHidenCategoriesByFatherID(getActivity(), backIDs.get(0));
        }else {
            queryCursor = POIsContract.CategoryEntry.getCategoriesByFatherID(getActivity(), backIDs.get(0));
            if(EDITABLE_TAG.endsWith("POIS")) {
                POIsButtonsVisibility();
            }
        }

        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);

        if (queryCursor.getCount() > 0) {
            categoriesListView.setAdapter(adapter);
            routeID = Integer.parseInt(backIDs.get(0));
            buttonsVisibility();

        } else {
            routeID = Integer.parseInt(backIDs.get(0));
            buttonsVisibility();
            if(backIDs.size()>0) {
                Toast.makeText(getActivity(), "There aren't more categories", Toast.LENGTH_SHORT).show();
                categoriesListView.setAdapter(adapter);
            }
        }

        String routeShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), backIDs.get(0));
        route.setText(routeShownName);

        if(notify.equals("POI")){
            showPOIsByCategory(routeID);
        }

        if(notify.equals("TOUR")){
            showToursByCategory(routeID);
        }

        //When user clicks on one category
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!tourIsWorking) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        int itemSelectedID = cursor.getInt(0);
                        backIDs.add(0, String.valueOf(itemSelectedID));
                        showCategoriesByLevel();
                    }
                }
            }
        });

        //When user long clicks on one category
        if(!notify.equals("POI") && !notify.equals("TOUR")) {
            categoriesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        int itemSelectedID = cursor.getInt(0);
                        dialog.show();
                        cancelButtonTreatment(cancel, dialog);
                        editButtonTreatment(String.valueOf(itemSelectedID), "CATEGORY", edit, dialog);
                        deleteButtonTreatment(itemSelectedID, POI_URI, Category_IDselection, "CATEGORY", getWhereClauseCategory("CATEGORY LEVEL"), null, delete, dialog);
                    }
                    return true;
                }
            });
        }

        return routeID;
    }
    private void showAllCategories(){

        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);

        Cursor queryCursor = POIsContract.CategoryEntry.getAllCategories(getActivity());

        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);
        categoriesListView.setAdapter(adapter);
        route.setText("/");

        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!tourIsWorking) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        int itemSelectedID = cursor.getInt(0);
//                    popupView = createAndShowItemSelectedPopup();
                        dialog.show();
                        cancelButtonTreatment(cancel, dialog);
                        editButtonTreatment(String.valueOf(itemSelectedID), "CATEGORY", edit, dialog);
                        deleteButtonTreatment(itemSelectedID, Category_URI, Category_IDselection, "CATEGORY", getWhereClauseCategory("ALL"), null, delete, dialog);
                    }
                }
            }
        });
    }
    private String getWhereClauseCategory(String type){
        if(type.equals("CATEGORY LEVEL")) {
            return POIsContract.CategoryEntry.COLUMN_FATHER_ID + " = " + backIDs.get(0);
        }else{
            return null;
        }
    }
    private void buttonsVisibility(){
        if (!backIDs.get(0).equals("0")) {
            if (!backIcon.isShown() && !backStartIcon.isShown()) {
                backIcon.setVisibility(View.VISIBLE);
                backStartIcon.setVisibility(View.VISIBLE);
            }
            setBackButtonTreatment();
            setBackToStartButtonTreatment();
        } else {
            if (backIcon.isShown() && backStartIcon.isShown()) { //&& backIDs.isEmpty()
                backIcon.setVisibility(View.GONE);
                backStartIcon.setVisibility(View.GONE);
            }
        }
    }
    private void setBackButtonTreatment(){
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIDs.remove(0);
                showCategoriesByLevel();
            }
        });
    }
    private void setBackToStartButtonTreatment(){
        backStartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIDs.clear();
                backIDs.add("0");
                showCategoriesByLevel();
            }
        });
    }
    private void updateSonsFatherIDAndShownName(String itemSelectedID, String itemSelectedFatherID){
        final String fatherShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), Integer.parseInt(itemSelectedFatherID));

        searchAndEditCategoriesSons(itemSelectedID, itemSelectedFatherID, fatherShownName);
    }
    private void searchAndEditCategoriesSons(String itemSelectedID, String fatherOfItemSelectedID, String shownNameFather){
        //we get the ID and Name of the pois whose father ID is itemSelectedID parameter.
        //On the first loop, it will be the ID from the sons of the poi we want to delete
        Cursor sonsIDCursor = POIsContract.CategoryEntry.getIDAndNameByFatherID(getActivity(),itemSelectedID);
        final int COLUMN_ID = 0, COLUMN_NAME = 1;
        //we initially could do an update of all the sons, without having to make the previous selection
        //and go updating one by one, better said: update from all rows whose fatherID equals itemIdSelected.
        //But we can't, because we also have to change the data of the sons of that sons we are talking about.
        //So, to do that, we take the sons of itemToDelete (itemSelectedID in the first loop of this function)
        //one by one and we check if they have more sons to change.

        //for each son
        if(sonsIDCursor.getCount()>0){
            while(sonsIDCursor.moveToNext()){
                //We change shownName and ID
                int sonID = sonsIDCursor.getInt(COLUMN_ID);
                String sonName = sonsIDCursor.getString(COLUMN_NAME);
                String finalShownName = shownNameFather + sonName + "/";

                int updatedRows = POIsContract.CategoryEntry.updateFatherIdAndShownNameByID(getActivity(), String.valueOf(sonID), fatherOfItemSelectedID, finalShownName);
                if(updatedRows == 1) {//if updated is correct done
                    searchAndEditShownNameOfCategoriesSons(sonID, finalShownName);
                }
            }
        }
    }
    private void searchAndEditShownNameOfCategoriesSons(int fatherID, String shownName){
        Cursor sonsIDCursor = POIsContract.CategoryEntry.getIDAndNameByFatherID(getActivity(), String.valueOf(fatherID));
        final int COLUMN_ID = 0, COLUMN_NAME = 1;

        while(sonsIDCursor.moveToNext()){
            //We change shownName and ID
            int sonID = sonsIDCursor.getInt(COLUMN_ID);
            String sonName = sonsIDCursor.getString(COLUMN_NAME);
            String finalShownName = shownName + sonName + "/";

            int updatedRows = POIsContract.CategoryEntry.updateShownNameByID(getActivity(), sonID, finalShownName);
            if(updatedRows == 1) {//if updated is correct done
                searchAndEditShownNameOfCategoriesSons(sonID, finalShownName);
            }
        }
    }

    /*POIS TREATMENT*/
    private void showPOIs(){

        POIsButtonsVisibility();
        notify = "POI";
        showCategoriesByLevel();
    }
    private void POIsButtonsVisibility(){
        if(EDITABLE_TAG.startsWith("ADMIN")) {
            //This 'if' statement is accessed when admin user creates a Tour.
            if(EDITABLE_TAG.equals("ADMIN/TOUR_POIS")){
                poisfragment.setPadding(0,0, 0, 10);
                additionLayout.setVisibility(View.GONE);

            }else {
                additionLayout.setVisibility(View.VISIBLE);
                createPOI.setVisibility(View.VISIBLE);
                createPOIhere.setVisibility(View.VISIBLE);
            }
        }else{
            additionLayout.setVisibility(View.GONE);
            if(createPOI.getVisibility() == View.VISIBLE){
                createPOI.setVisibility(View.GONE);
            }
            if(createPOIhere.getVisibility() == View.VISIBLE){
                createPOIhere.setVisibility(View.GONE);
            }
        }
        if(EDITABLE_TAG.endsWith("POIS")) {
            seeingOptions.setVisibility(View.GONE);
            poisListViewTittle.setText("POIs");
        }
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);
        poisListViewTittle.setVisibility(View.VISIBLE);
        categoriesListView.setVisibility(View.VISIBLE);
    }
    private int showPOIsByCategory(final int categoryID){

        Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")){
            queryCursor = POIsContract.POIEntry.getNotHidenPOIsByCategory(getActivity(), String.valueOf(categoryID));
        }else{
            queryCursor = POIsContract.POIEntry.getPOIsByCategory(getActivity(), String.valueOf(categoryID));
        }

        String routeShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), String.valueOf(categoryID));
        route.setText(routeShownName);

        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("POI");

        poisListView.setVisibility(View.VISIBLE);
        poisListView.setAdapter(adapterPOI);

        poisListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int itemSelectedID = cursor.getInt(0);
                if (EDITABLE_TAG.startsWith("ADMIN")) {
                    if (EDITABLE_TAG.endsWith("/TOUR_POIS")) {
                        String completeName = cursor.getString(1);
                        delete.setVisibility(View.GONE);
                        edit.setVisibility(View.GONE);
                        FloatingActionButton add = (FloatingActionButton) dialogView.findViewById(R.id.add_poi);
                        add.setVisibility(View.VISIBLE);
                        dialog.show();
                        cancelButtonTreatment(cancel, dialog);
                        addTourPOIsButtonTreatment(itemSelectedID, completeName, add);
                    } else {
//                        popupView = createAndShowItemSelectedPopup();
                        dialog.show();
                        cancelButtonTreatment(cancel, dialog);
                        editButtonTreatment(String.valueOf(itemSelectedID), "POI", edit, dialog);
                        String whereClauseForRefreshing = POIsContract.POIEntry.COLUMN_CATEGORY_ID + " = " + String.valueOf(categoryID);
                        deleteButtonTreatment(itemSelectedID, POI_URI, POI_IDselection, "POI", getWhereClauseCategory("CATEGORY LEVEL"), whereClauseForRefreshing, delete, dialog);
                    }
                } else {//IF BASIC USER CLICKS ON ONE POI...
                    try {
                        HashMap<String, String> poi = getPOIData(itemSelectedID);
                        String command = buildCommand(poi);
                        try {
                            setConnectionWithLiquidGalaxy(command);
                        } catch (JSchException e) {
                            Toast.makeText(getActivity(), "Error connecting with Liquid Galaxy system. Try changing username, password, port or the host ip", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Error. " + ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return queryCursor.getCount();
    }
    private HashMap<String, String> getPOIData(int id) throws Exception {
        Cursor c = POIsContract.POIEntry.getPOIByID(getActivity(), String.valueOf(id));
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
    private String updatePoiSonsCategoryID(String itemSelectedID){
        String fatherID = POIsContract.CategoryEntry.getFatherIdByID(getActivity(), itemSelectedID);
        int updatedRows = POIsContract.POIEntry.updateCategoryIDByCategoryID(getActivity(), itemSelectedID, fatherID);
        Toast.makeText(getActivity(), String.valueOf(updatedRows), Toast.LENGTH_SHORT).show();
        return fatherID;
    }
    private int deletePOIsOfTours(String itemSelectedID){
        return POIsContract.TourPOIsEntry.deleteByPoiID(getActivity(), itemSelectedID);
    }

    /*TOURS TREATMENT*/
    private void showTOURs(){

        if(EDITABLE_TAG.startsWith("ADMIN")) {
            additionLayout.setVisibility(View.VISIBLE);
            createTour.setVisibility(View.VISIBLE);
            createTourhere.setVisibility(View.GONE);
        }else{
            additionLayout.setVisibility(View.VISIBLE);
        }
        poisListViewTittle.setVisibility(View.VISIBLE);
        poisListViewTittle.setText("Tours");
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);
        showAllTours();
        seeingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seeingOptions.getText().toString().equals("See elements by category")) { //vull veure-les per categoria
                    poisListViewTittle.setText("Tours");
                    seeingOptions.setVisibility(View.VISIBLE);
                    seeingOptions.setText("See all elements");
                    if (EDITABLE_TAG.startsWith("ADMIN")) {
                        createTourhere.setVisibility(View.VISIBLE);
                    }
                    categoriesListView.setVisibility(View.VISIBLE);
                    notify = "TOUR";
                    showCategoriesByLevel();

                } else {
                    seeingOptions.setText("See elements by category");
                    backIDs.clear();
                    backIDs.add("0");
                    if (EDITABLE_TAG.startsWith("ADMIN")) {
                        createTourhere.setVisibility(View.GONE);
                    }
                    poisListViewTittle.setVisibility(View.VISIBLE);
                    backIcon.setVisibility(View.GONE);
                    backStartIcon.setVisibility(View.GONE);
                    showAllTours();
                }
            }
        });

    }
    private void addTourPOIsButtonTreatment(final int itemSelectedID, final String completeName, FloatingActionButton add){

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createORupdate.equals("update")) {
                    UpdateItemFragment.setPOItoTourPOIsList(String.valueOf(itemSelectedID), completeName);
                    dialog.dismiss();
                } else {
                    try {
                        CreateItemFragment.setPOItoTourPOIsList(String.valueOf(itemSelectedID), completeName);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            }
        });
    }
    private void showAllTours(){
        final Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")) {
            queryCursor = POIsContract.TourEntry.getAllNotHidenTours(getActivity());
        }else{//ADMIN
            queryCursor = POIsContract.TourEntry.getAllTours(getActivity());
        }
        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("TOUR");

        route.setText("/");

        poisListView.setVisibility(View.VISIBLE);
        categoriesListView.setVisibility(View.GONE);
        poisListView.setAdapter(adapterPOI);

        poisListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!tourIsWorking) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    int itemSelectedID = cursor.getInt(0);
                    if (EDITABLE_TAG.startsWith("ADMIN")) {
                        dialog.show();
                        cancelButtonTreatment(cancel, dialog);
                        editButtonTreatment(String.valueOf(itemSelectedID), "TOUR", edit, dialog);
                        deleteButtonTreatment(itemSelectedID, TOUR_URI, TOUR_IDselection, "TOUR", null, null, delete, dialog);
                    } else {

                        tour = new LiquidGalaxyTourView();
                        tour.setActivity(getActivity());
                        additionLayout.setVisibility(View.VISIBLE);
                        tour.execute(String.valueOf(itemSelectedID));
                        stopButton.setVisibility(View.VISIBLE);
                        stopButton.setClickable(true);

                        invalidateOtherClickableElements();
                        stopButtonBehaviour(tour);
                    }
                }
            }
        });
    }
    public static void resetTourSettings(){

        tour.cancel(true);
        stopButton.setVisibility(View.GONE);
        stopButton.setClickable(false);
        tourIsWorking = false;

        showStopAlert();
    }
    private static void showStopAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
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
    public static boolean getTourState(){
        return tourIsWorking;
    }
    private void invalidateOtherClickableElements() {
        tourIsWorking = true;
    }
    private void stopButtonBehaviour(final LiquidGalaxyTourView tour) {
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert(stopButton, tour);
            }
        });
    }
    private void showAlert(final android.support.design.widget.FloatingActionButton stopButton, final LiquidGalaxyTourView tour){
        // prepare the alert box
        AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());

        // set the message to display
        alertbox.setMessage("Are you sure to stop the Tour view?");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                tour.cancel(true);
                stopButton.setVisibility(View.GONE);
                stopButton.setClickable(false);
                tourIsWorking = false;
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
    private int showToursByCategory(final int categoryID){

        final Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")) {
            queryCursor = POIsContract.TourEntry.getNotHidenToursByCategory(getActivity(), String.valueOf(categoryID));
        }else{//ADMIN
            queryCursor = POIsContract.TourEntry.getToursByCategory(getActivity(), String.valueOf(categoryID));
        }
        String routeShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), String.valueOf(categoryID));
        route.setText(routeShownName);

        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("TOUR");

        poisListView.setVisibility(View.VISIBLE);
        poisListView.setAdapter(adapterPOI);

        poisListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!tourIsWorking) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    int itemSelectedID = cursor.getInt(0);
                    if (EDITABLE_TAG.startsWith("ADMIN")) {
                        dialog.show();
                        cancelButtonTreatment(cancel, dialog);
                        editButtonTreatment(String.valueOf(itemSelectedID), "TOUR", edit, dialog);
                        String whereClauseForRefreshing = POIsContract.TourEntry.COLUMN_CATEGORY_ID + " = " + String.valueOf(categoryID);
                        deleteButtonTreatment(itemSelectedID, TOUR_URI, TOUR_IDselection, "TOUR", getWhereClauseCategory("CATEGORY LEVEL"), whereClauseForRefreshing, delete, dialog);
                    } else {
                        tour = new LiquidGalaxyTourView();
                        tour.setActivity(getActivity());
                        additionLayout.setVisibility(View.VISIBLE);
                        tour.execute(String.valueOf(itemSelectedID));
                        stopButton.setVisibility(View.VISIBLE);
                        stopButton.setClickable(true);


                        invalidateOtherClickableElements();
                        stopButtonBehaviour(tour);
                    }
                }
            }
        });

        return queryCursor.getCount();
    }
    private Dialog getDialogByView(View v){
            // prepare the alert box
        Dialog dialog = new Dialog(getActivity());
        dialog.setTitle("Item Options");
        dialog.setContentView(v);

        return dialog;
    }

    /*OTHER UTILITIES*/
    private void cancelButtonTreatment(android.support.design.widget.FloatingActionButton cancel, final Dialog dialog){

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private void editButtonTreatment(final String itemSelectedId, final String type, android.support.design.widget.FloatingActionButton edit, final Dialog dialog){

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent updateCategoryIntent = new Intent(getActivity(), UpdateItemActivity.class);
                if(type.equals("POI")){
                    updateCategoryIntent.putExtra("UPDATE_TYPE", type);
                    updateCategoryIntent.putExtra("ITEM_ID", itemSelectedId);
                }else if(type.equals("TOUR")){
                    updateCategoryIntent.putExtra("UPDATE_TYPE", type);
                    updateCategoryIntent.putExtra("ITEM_ID", itemSelectedId);
                }else {
                    updateCategoryIntent.putExtra("UPDATE_TYPE", type);
                    updateCategoryIntent.putExtra("ITEM_ID", itemSelectedId);
                }
                startActivity(updateCategoryIntent);
            }
        });
    }
    private void deleteButtonTreatment(final int itemSelectedID, final Uri uri, final String whereClause, final String itemName,
                                       final String categoryRefreshListSelection, final String POIorTOURRefreshSelection, FloatingActionButton delete, final Dialog dialog){

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(itemName.equals("CATEGORY")) {
                    String itemSelectedFatherID = updatePoiSonsCategoryID(String.valueOf(itemSelectedID));
                    updateSonsFatherIDAndShownName(String.valueOf(itemSelectedID), itemSelectedFatherID);
                }
                int deletedRows = POIsContract.delete(getActivity(), uri, whereClause, new String[]{String.valueOf(itemSelectedID)});
                if(deletedRows > 0) {
                    refreshPOIsListView(uri, itemName, categoryRefreshListSelection, POIorTOURRefreshSelection);
                    if(itemName.equals("POI")){
                        Toast.makeText(getActivity(), deletePOIsOfTours(String.valueOf(itemSelectedID)) + " rows deleted", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    Toast.makeText(getActivity(), String.valueOf(deletedRows) + " rows deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private View createAndShowItemSelectedPopup(){
        final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.popup_poi_list_item_selected, null);

        popupPoiSelected = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupPoiSelected.setTouchable(true);
        popupPoiSelected.setFocusable(true);
        popupPoiSelected.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        return popupView;
    }
    private void refreshPOIsListView(Uri uri, String itemName, String whereClauseCategory, String whereClausePOIorTOUR){

        if(!uri.toString().endsWith("category")) {
            Cursor queryCursor = getActivity().getContentResolver().query(uri, null, whereClausePOIorTOUR, null, null);
            adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
            adapterPOI.setItemName(itemName);
            poisListView.setAdapter(adapterPOI);
        }
        Cursor queryCursor = POIsContract.CategoryEntry.getCategoriesForRefreshing(getActivity(), whereClauseCategory);
        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);
        categoriesListView.setAdapter(adapter);
    }
    private String setConnectionWithLiquidGalaxy(String command) throws JSchException {

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

        return baos.toString();
    }
    //When user decide to create an item inside the category he/she is watching in the screen.
    private void setNewItemHereButtonBehaviour(){
        //Depending on the button clicked, the user will see an interface or other, depending on the type of the item
        createCategoryhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "CATEGORY/HERE");
                startActivity(createPoiIntent);
            }
        });

        createPOIhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "POI/HERE");
                startActivity(createPoiIntent);
            }
        });

        createTourhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "TOUR/HERE");
                startActivity(createPoiIntent);
            }
        });
    }
    //When user decide to create an item.
    private void setNewItemButtonBehaviour(){

        createCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createCategoryIntent = new Intent(getActivity(), CreateItemActivity.class);
                createCategoryIntent.putExtra("CREATION_TYPE", "CATEGORY");
                startActivity(createCategoryIntent);
            }
        });

        createPOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "POI");
                startActivity(createPoiIntent);
            }
        });

        createTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent createTourIntent = new Intent(getActivity(), CreateItemActivity.class);
                createTourIntent.putExtra("CREATION_TYPE", "TOUR");
                startActivity(createTourIntent);
            }
        });
    }
}