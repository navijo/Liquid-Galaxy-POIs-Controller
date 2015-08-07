package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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

    private CategoriesAdapter adapter;
    private POIsAdapter adapterPOI;
    private ListView poisListView, categoriesListView;
//    private DynamicListView poisListView;
    private PopupWindow popupPoiSelected;
    private View popupView, poisView, tourPOIsView;
    private static View adminView;
    private final String POI_IDselection = POIsContract.POIEntry._ID + " = ?";
    private final String TOUR_IDselection = POIsContract.TourEntry._ID + " = ?";
    private final String Category_IDselection = POIsContract.CategoryEntry._ID + " = ?";
    private final Uri POI_URI = POIsContract.POIEntry.CONTENT_URI;
    private final Uri TOUR_URI = POIsContract.TourEntry.CONTENT_URI;
    private final Uri Category_URI = POIsContract.CategoryEntry.CONTENT_URI;

    private String EDITABLE_TAG, notify, createORupdate = "";
    private ImageView backIcon, backStartIcon;
    private List<String> backIDs = new ArrayList<String>(){{
            add("0");
    }};
    private TextView seeingOptions, poisListViewTittle, route;
    public static int routeID = 0;
    private static Button category_here, tour_here, poi_here;

    public POISFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        EDITABLE_TAG = this.getTag();
        if(getArguments() !=null) {
            createORupdate = getArguments().getString("createORupdate");
        }
        poisView = inflater.inflate(R.layout.fragment_pois, container, false);

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

        if(EDITABLE_TAG.endsWith("POIS")){
            showPOIs();
        }
        if(EDITABLE_TAG.endsWith("TOURS")){
            showTOURs();
        }
        if(EDITABLE_TAG.equals("ADMIN/CATEGORIES")){
            showCategories();
        }
    }

    /*CATEGORIES TREATMENT */
    private void showCategories(){

        category_here.setVisibility(View.GONE);
        poisListView.setVisibility(View.GONE);
        showAllCategories();

        seeingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seeingOptions.getText().toString().equals("See elements by category")){
                    seeingOptions.setText("See all elements");
                    category_here.setVisibility(View.VISIBLE);
                    notify = "CATEGORY";
                    showCategoriesByLevel();

                }else{
                    seeingOptions.setText("See elements by category");
                    backIDs.clear();
                    backIDs.add("0");
                    category_here.setVisibility(View.GONE);
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
            if(showPOIsByCategory(routeID) == 0){
                Toast.makeText(getActivity(), "There are no POIs inside this category", Toast.LENGTH_LONG).show();
            }
        }

        if(notify.equals("TOUR")){
            if(showToursByCategory(routeID) == 0){
                Toast.makeText(getActivity(), "There are no TOURs inside this category", Toast.LENGTH_LONG).show();
            }
        }

        //When user clicks on one category
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int itemSelectedID = cursor.getInt(0);
                    backIDs.add(0, String.valueOf(itemSelectedID));
                    showCategoriesByLevel();
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
                        popupView = createAndShowItemSelectedPopup();
                        cancelButtonTreatment(popupView);
                        editButtonTreatment(String.valueOf(itemSelectedID), "CATEGORY");
                        deleteButtonTreatment(itemSelectedID, POI_URI, Category_IDselection, "CATEGORY", getWhereClauseCategory("CATEGORY LEVEL"), null);
                    }
                    return true;
                }
            });
        }

        return routeID;
    }
    private void showAllCategories(){

//        back.setVisibility(View.GONE);
//        backStart.setVisibility(View.GONE);
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);

        Cursor queryCursor = POIsContract.CategoryEntry.getAllCategories(getActivity());
        //Cursor queryCursor = POIsContract.CategoryEntry.getAllNotHidenCategories(getActivity());

        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);
        categoriesListView.setAdapter(adapter);
        route.setText("/");

        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int itemSelectedID = cursor.getInt(0);
                    popupView = createAndShowItemSelectedPopup();
                    cancelButtonTreatment(popupView);
                    editButtonTreatment(String.valueOf(itemSelectedID), "CATEGORY");
                    deleteButtonTreatment(itemSelectedID, Category_URI, Category_IDselection, "CATEGORY", getWhereClauseCategory("ALL"), null);
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
        Toast.makeText(getActivity(),"GOOOOOD",Toast.LENGTH_SHORT).show();

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

        if(EDITABLE_TAG.startsWith("ADMIN")) {
            poi_here.setVisibility(View.VISIBLE);
        }
        seeingOptions.setVisibility(View.GONE);
        poisListViewTittle.setText("List of POIs");
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);
        poisListViewTittle.setVisibility(View.VISIBLE);
        categoriesListView.setVisibility(View.VISIBLE);
        notify = "POI";
        showCategoriesByLevel();
    }
    private void showAllPois(){
        final Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")) {
            queryCursor = POIsContract.POIEntry.getAllNotHidenPOIs(getActivity());
        }else{//ADMIN
            queryCursor = POIsContract.POIEntry.getAllPOIs(getActivity());
        }
        route.setText("/");
        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("POI");

        poisListView.setVisibility(View.VISIBLE);
        categoriesListView.setVisibility(View.GONE);
        //---------------------
//        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(adapter);
//        animationAdapter.setAbsListView(poisListView);
//        poisListView.enableDragAndDrop();
//        poisListView.setDraggableManager(new TouchViewDraggableManager(R.id.poi_list_item_textview));
        //---------------------

        poisListView.setAdapter(adapterPOI);

        poisListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int itemSelectedID = cursor.getInt(0);
                if (EDITABLE_TAG.startsWith("ADMIN")) {
                    if(EDITABLE_TAG.endsWith("/TOUR_POIS")){
                        String completeName = cursor.getString(1);
                        createAndShowTourPOIsView();
                        cancelButtonTreatment(tourPOIsView);
                        addTourPOIsButtonTreatment(itemSelectedID, completeName);
                    }else {//ends with only /POIS
                        createAndShowItemSelectedPopup();
                        cancelButtonTreatment(popupView);
                        editButtonTreatment(String.valueOf(itemSelectedID), "POI");
                        deleteButtonTreatment(itemSelectedID, POI_URI, POI_IDselection, "POI", null, null);
                    }
                }else{
                    Toast.makeText(getActivity(), cursor.getString(1), Toast.LENGTH_SHORT).show();
                }
                }
        });
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
                if(EDITABLE_TAG.startsWith("ADMIN")) {
                    if(EDITABLE_TAG.endsWith("/TOUR_POIS")) {
                        String completeName = cursor.getString(1);
                        createAndShowTourPOIsView();
                        cancelButtonTreatment(tourPOIsView);
                        addTourPOIsButtonTreatment(itemSelectedID, completeName);
                    }else{
                        popupView = createAndShowItemSelectedPopup();
                        cancelButtonTreatment(popupView);
                        editButtonTreatment(String.valueOf(itemSelectedID), "POI");
                        String whereClauseForRefreshing = POIsContract.POIEntry.COLUMN_CATEGORY_ID + " = " + String.valueOf(categoryID);
                        deleteButtonTreatment(itemSelectedID, POI_URI, POI_IDselection, "POI", getWhereClauseCategory("CATEGORY LEVEL"), whereClauseForRefreshing);
                    }
                }else{//IF BASIC USER CLICKS ON ONE POI...
                    try {
                        HashMap<String, String> poi = getPOIData(itemSelectedID);
                        String command = buildCommand(poi);
                        try {
                            setConnectionWithLiquidGalaxy(command);
                        } catch (JSchException e) {
                            Toast.makeText(getActivity(),"Error connecting with Liquid Galaxy system. Try changing username, password, port or the host ip", Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception ex){
                        Toast.makeText(getActivity(),"Error. " + ex.getMessage().toString(), Toast.LENGTH_LONG).show();
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
            tour_here.setVisibility(View.GONE);
        }
        poisListViewTittle.setText("List of TOURs");
//        back.setVisibility(View.GONE);
//        backStart.setVisibility(View.GONE);
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);
        showAllTours();
        seeingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seeingOptions.getText().toString().equals("See elements by category")){ //vull veure-les per categoria
                    seeingOptions.setText("See all elements");
                    if(EDITABLE_TAG.startsWith("ADMIN")) {
                        tour_here.setVisibility(View.VISIBLE);
                    }
                    poisListViewTittle.setVisibility(View.VISIBLE);
                    categoriesListView.setVisibility(View.VISIBLE);
                    notify = "TOUR";
                    showCategoriesByLevel();

                }else{
                    seeingOptions.setText("See elements by category");
                    backIDs.clear();
                    backIDs.add("0");
                    if(EDITABLE_TAG.startsWith("ADMIN")) {
                        tour_here.setVisibility(View.GONE);
                    }
                    poisListViewTittle.setVisibility(View.GONE);
//                    back.setVisibility(View.GONE);
//                    backStart.setVisibility(View.GONE);
                    backIcon.setVisibility(View.GONE);
                    backStartIcon.setVisibility(View.GONE);
                    showAllTours();
                }
            }
        });

    }
    private void createAndShowTourPOIsView(){
        final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tourPOIsView = layoutInflater.inflate(R.layout.popup_tour_pois_list, null);

        popupPoiSelected = new PopupWindow(tourPOIsView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupPoiSelected.setTouchable(true);
        popupPoiSelected.setFocusable(true);
        popupPoiSelected.showAtLocation(tourPOIsView, Gravity.CENTER, 0, 0);
    }
    private void addTourPOIsButtonTreatment(final int itemSelectedID, final String completeName){
        Button addButton = (Button) tourPOIsView.findViewById(R.id.add_tour_poi_listview);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createORupdate.equals("update")) {
                    UpdateItemFragment.setPOItoTourPOIsList(String.valueOf(itemSelectedID), completeName);
                    popupPoiSelected.dismiss();
                }else{
                    try {
                        CreateItemFragment.setPOItoTourPOIsList(String.valueOf(itemSelectedID), completeName);
                    }catch (Exception e){
                        Toast.makeText(getActivity(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                    popupPoiSelected.dismiss();
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
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if(EDITABLE_TAG.startsWith("ADMIN")) {
                    int itemSelectedID = cursor.getInt(0);
                    popupView = createAndShowItemSelectedPopup();
                    cancelButtonTreatment(popupView);
                    editButtonTreatment(String.valueOf(itemSelectedID), "TOUR");
                    deleteButtonTreatment(itemSelectedID, TOUR_URI, TOUR_IDselection, "TOUR", null, null);
                }else{
                    Toast.makeText(getActivity(), cursor.getString(1), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int itemSelectedID = cursor.getInt(0);
                if (EDITABLE_TAG.startsWith("ADMIN")) {
                    popupView = createAndShowItemSelectedPopup();
                    cancelButtonTreatment(popupView);
                    editButtonTreatment(String.valueOf(itemSelectedID), "TOUR");
                    String whereClauseForRefreshing = POIsContract.TourEntry.COLUMN_CATEGORY_ID + " = " + String.valueOf(categoryID);
                    deleteButtonTreatment(itemSelectedID, TOUR_URI, TOUR_IDselection, "TOUR", getWhereClauseCategory("CATEGORY LEVEL"), whereClauseForRefreshing);
                    Toast.makeText(getActivity(), cursor.getString(1), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(), cursor.getString(1), Toast.LENGTH_SHORT).show();
                }
            }});

        return queryCursor.getCount();
    }

    /*OTHER UTILITIES*/
    public static void setAdminView(View view){
        adminView = view;
        category_here = (Button) adminView.findViewById(R.id.new_category_here);
        poi_here = (Button) adminView.findViewById(R.id.new_poi_here);
        tour_here = (Button) adminView.findViewById(R.id.new_tour_here);
    }
    private void cancelButtonTreatment(View view){

        final Button cancelButton = (Button) view.findViewById(R.id.cancel_poi_selection);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupPoiSelected.dismiss();
            }
        });
    }
    private void editButtonTreatment(final String itemSelectedId, final String type){

        final Button updateButton = (Button) popupView.findViewById(R.id.edit_poi);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupPoiSelected.dismiss();
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
                                       final String categoryRefreshListSelection, final String POIorTOURRefreshSelection){

        final Button deleteButton = (Button) popupView.findViewById(R.id.delete_poi);
        deleteButton.setOnClickListener(new View.OnClickListener() {
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
                    popupPoiSelected.dismiss();
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
        //Cursor queryCursor = getActivity().getContentResolver().query(uri, null, whereClauseCategory, null, null);
        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);
        categoriesListView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
        //PROBAR NOTIFYDATASETCHANGED DE ADAPTER
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
