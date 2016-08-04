package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.beans.TourPOI;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.utils.LGUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This is the most important fragment of the app. It contains all necessary elements to have a list
 * with the corresponding elements. Let's the user see a list of categories, POIs and Tours at the same time
 * that let him operate with this elements. It is thre responible of the items list views and its
 * management.
 */
public class POISFragment extends Fragment {

    public static LiquidGalaxyTourView tour;
    public static int routeID = 0;
    private static FragmentActivity currentActivity;
    private static FloatingActionButton stopButton;
    private static boolean tourIsWorking = false;
    private final String POI_IDselection = POIsContract.POIEntry._ID + " = ?";
    private final String TOUR_IDselection = POIsContract.TourEntry._ID + " = ?";
    private final String Category_IDselection = POIsContract.CategoryEntry._ID + " = ?";
    private final Uri POI_URI = POIsContract.POIEntry.CONTENT_URI;
    private final Uri TOUR_URI = POIsContract.TourEntry.CONTENT_URI;
    private final Uri Category_URI = POIsContract.CategoryEntry.CONTENT_URI;
    Session session;
    private CategoriesAdapter adapter;
    private POIsAdapter adapterPOI;
    private ListView poisListView, categoriesListView;
    private View poisView, dialogView;
    private Dialog dialog;
    private String EDITABLE_TAG, notify, createORupdate = "";
    private ImageView backIcon, backStartIcon;
    private List<String> backIDs = new ArrayList<String>(){{
        add("0");
    }};
    private TextView seeingOptions, poisListViewTittle, route, categories_tittle;
    private LinearLayout additionLayout, poisfragment;
    private FloatingActionButton createPOI, createPOIhere, createTour, createCategory, createTourhere,createCategoryhere,cancel,edit,delete;

    public POISFragment() {
    }

    public static void resetTourSettings() {

        tour.cancel(true);
        stopButton.setVisibility(View.GONE);
        stopButton.setClickable(false);
        tourIsWorking = false;

        showStopAlert();
    }

    private static void showStopAlert() {
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

    public static boolean getTourState() {
        return tourIsWorking;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getArguments() !=null) {
            //this argument works for trying to a posterior execution of Create or Update fragment.
            createORupdate = getArguments().getString("createORupdate");
            //depending on the value of this argument, the algorythm will decide to show POIs, Tours
            //or Categories and at the same time, depending on the origin of the call (from a basic user
            // or an admin user), will enable or disable some functionalities
            EDITABLE_TAG = getArguments().getString("EDITABLE");
        }

        poisView = inflater.inflate(R.layout.fragment_pois, container, false);
        currentActivity = getActivity();

        PriorPreparation();

        return poisView;
    }


    private void PriorPreparation(){

        poisListView = (ListView) poisView.findViewById(R.id.pois_listview); //List with pois or tours
        categoriesListView = (ListView) poisView.findViewById(R.id.categories_listview);//List with categories
        backIcon = (ImageView) poisView.findViewById(R.id.back_icon);//comes back to the previous category
        backStartIcon = (ImageView) poisView.findViewById(R.id.back_start_icon);//comes back to the initial category
        seeingOptions = (TextView) poisView.findViewById(R.id.see_all_or_by_category);//by clicking on it, lets the user see all elements or classified by categories
        poisListViewTittle = (TextView) poisView.findViewById(R.id.pois_tittle_listview);
        route = (TextView) poisView.findViewById(R.id.fragment_pois_route);//categories route
        poisfragment = (LinearLayout) poisView.findViewById(R.id.pois_xml_fragment);//content of elements representing all the fragment
        stopButton = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.tour_stop);//stops tour execution on LG
        categories_tittle = (TextView) poisView.findViewById(R.id.categories_textview);

        additionLayout = (LinearLayout) poisView.findViewById(R.id.addition_buttons_layout);//layout containging buttons to add items
        createPOI = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_poi);
        createCategory = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_category);
        createTour = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_tour);
        createPOIhere = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_poi_here);
        createCategoryhere = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_category_here);
        createTourhere = (android.support.design.widget.FloatingActionButton) poisView.findViewById(R.id.new_tour_here);

        dialogView = getLayoutInflater(getArguments()).inflate(R.layout.dialog_item_options, null); //In the tours creation process, it lets the user add POIs. when the elements selection inside Admin section, lets edit or update them.
        dialog = getDialogByView(dialogView);
        cancel = (FloatingActionButton) dialogView.findViewById(R.id.cancel_poi_selection);
        edit = (FloatingActionButton) dialogView.findViewById(R.id.edit_poi);
        delete = (FloatingActionButton) dialogView.findViewById(R.id.delete_poi);

        screenSizeTreatment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GetSessionTask getSessionTask = new GetSessionTask();
        getSessionTask.execute();
    }

    /* Depending on the screen size, it will set different font size.*/
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
        setNewItemButtonBehaviour();

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

    /*------------CATEGORIES TREATMENT------------*/
    private void showCategories(){
        categoryInitialVisibility();
        showAllCategories();
        changeToAllCategoriesOrClassifiedCategoriesTextViewBehaviour();
    }

    private void changeToAllCategoriesOrClassifiedCategoriesTextViewBehaviour(){
        seeingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seeingOptions.getText().toString().equals("See elements by category")) {//when user wants to see them classified by other categories
                    categoriesByCategoriesElementsVisibility();
                    showCategoriesByLevel();

                } else {//when user wants to see all the categories
                    allCategoriesElementsVisibility();
                    showAllCategories();
                }
            }
        });
    }

    private void categoryInitialVisibility(){
        additionLayout.setVisibility(View.VISIBLE);
        createCategory.setVisibility(View.VISIBLE);
        createCategoryhere.setVisibility(View.GONE);
        poisListView.setVisibility(View.GONE);
    }

    private void allCategoriesElementsVisibility(){
        seeingOptions.setText("See elements by category");
        backIDs.clear();
        backIDs.add("0");
        createCategoryhere.setVisibility(View.GONE);
    }

    private void categoriesByCategoriesElementsVisibility(){
        seeingOptions.setText("See all elements");
        createCategoryhere.setVisibility(View.VISIBLE);
        notify = "CATEGORY";
    }

    /*Show the categories classified by other categories.
    * The following six methods are part of the showing categories by categories method*/
    private int showCategoriesByLevel() {

        Cursor queryCursor = getCategoriesCursor();
        showCategoriesOnScreen(queryCursor);
        updateRouteAndPOIsOrToursViews();
        categoriesItemClickedBehaviour();
        categoriesItemLongClickedBehaviour();

        return routeID;
    }

    private Cursor getCategoriesCursor(){
        Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")){
            //we get only the categories that the admin user wants to be shown on the app screen and have father category ID the once of the parameters.
            queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), backIDs.get(0));
        }else {
            //we get the categories that their parent category is the same that the once of the parameters.
            queryCursor = POIsContract.CategoryEntry.getCategoriesByFatherID(getActivity(), backIDs.get(0));
            if(EDITABLE_TAG.endsWith("POIS")) {
                POIsButtonsVisibility();
            }
        }
        return queryCursor;
    }

    private void showCategoriesOnScreen(Cursor queryCursor){
        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);

        if (queryCursor.getCount() > 0) {
            categoriesListView.setAdapter(adapter);
            routeID = Integer.parseInt(backIDs.get(0)); // we get the ID of the current Category shown
            buttonsVisibility();//management of buttons visibility

        } else {
            routeID = Integer.parseInt(backIDs.get(0));
            buttonsVisibility();
            if(backIDs.size()>0) {
                categoriesListView.setAdapter(adapter);
            }
        }
    }

    private void updateRouteAndPOIsOrToursViews(){
        String routeShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), Integer.parseInt(backIDs.get(0)));
        route.setText(routeShownName);

        //Shows the POIS LIST
        if(notify.equals("POI")){
            showPOIsByCategory(routeID);
        }

        //SHOWS THE TOURS LIST
        if(notify.equals("TOUR")){
            showToursByCategory(routeID);
        }
    }

    private void categoriesItemClickedBehaviour(){
        //When user clicks on one category
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!tourIsWorking) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);//gets the category selected
                    if (cursor != null) {
                        int itemSelectedID = cursor.getInt(0);
                        backIDs.add(0, String.valueOf(itemSelectedID));
                        //this method is call to see AGAIN the categories list. However, the view will
                        //correspond to the categories inside the current category just clicked.
                        showCategoriesByLevel();
                    }
                }
            }
        });
    }

    private void categoriesItemLongClickedBehaviour(){
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
    }

    /*Show all the categories*/
    private void showAllCategories(){

        buttonsAndCategoriesListVisibility();
        allCategoriesListItemClickedBehaviour();
    }

    private void buttonsAndCategoriesListVisibility(){
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);

        Cursor queryCursor = POIsContract.CategoryEntry.getAllCategories(getActivity());

        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);
        categoriesListView.setAdapter(adapter);
        route.setText("/");
    }

    private void allCategoriesListItemClickedBehaviour() {
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!tourIsWorking) {
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

    /*Other Categories utilities to help its correct and effective view.*/
    //This method is called when the application need to get the 'subcategories' of another category when accessing on data base.
    private String getWhereClauseCategory(String type){
        if(type.equals("CATEGORY LEVEL")) {
            return POIsContract.CategoryEntry.COLUMN_FATHER_ID + " = " + backIDs.get(0);
        }else{
            return null;
        }
    }

    //This method decides to show or not the Back and Back Home button, depending where the user is navigating.
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

    /*The following two methods decides the behaviour of Back and Back Home buttons when they are clicked.*/
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

    //The following three methods are few complex because are called when some category is deleted by the user.
    //These algorythms gets the subCategories and subPOIs of the just deleted category and locate them above category.
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

    //This method is called when one category is deleted, so it have to update the POI field called Category ID.
    private String updatePoiSonsCategoryID(String itemSelectedID){
        String fatherID = POIsContract.CategoryEntry.getFatherIdByID(getActivity(), itemSelectedID);
        int updatedRows = POIsContract.POIEntry.updateCategoryIDByCategoryID(getActivity(), itemSelectedID, fatherID);
        Toast.makeText(getActivity(), String.valueOf(updatedRows), Toast.LENGTH_SHORT).show();
        return fatherID;
    }

    /*------------POIS TREATMENT------------*/
    private void showPOIs(){

        POIsButtonsVisibility();//Set the buttons visibility for getting a correct and effective pois list view.
        notify = "POI";//with this field, the following method will know what kind of elements (POIs or Tours) have to show.
        showCategoriesByLevel();//Shows the categories classified because when one category is clicked, the list of POIs of its category is shown
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

    /*The following five methods show the POIs on the screens and sets the different possibles behaviours of one item clicked*/
    private int showPOIsByCategory(final int categoryID){

        Cursor queryCursor = getPOIsAndShowThem(categoryID);
        poisListItemClickedBehaviour(categoryID);

        return queryCursor.getCount();
    }

    private Cursor getPOIsAndShowThem(int categoryID){
        Cursor queryCursor;
        //The same behaviour than in categories treatment
        if(EDITABLE_TAG.startsWith("USER")){
            queryCursor = POIsContract.POIEntry.getNotHiddenPOIsByCategory(getActivity(), String.valueOf(categoryID));
        }else{
            queryCursor = POIsContract.POIEntry.getPOIsByCategory(getActivity(), String.valueOf(categoryID));
        }
        String routeShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), categoryID);
        route.setText(routeShownName);

        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("POI");
        poisListView.setVisibility(View.VISIBLE);
        poisListView.setAdapter(adapterPOI);

        return queryCursor;
    }

    private void poisListItemClickedBehaviour(final int categoryID) {
        poisListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int itemSelectedID = cursor.getInt(0);
                if (EDITABLE_TAG.startsWith("ADMIN")) {
                    clickedByAdminBehaviour(itemSelectedID, cursor, categoryID);

                } else {//IF BASIC USER CLICKS ON ONE POI...
                    clickedByBasicUserBehaviour(itemSelectedID);
                }
            }
        });
    }

    private void clickedByAdminBehaviour(int itemSelectedID, Cursor cursor, int categoryID){
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
            dialog.show();
            cancelButtonTreatment(cancel, dialog);
            editButtonTreatment(String.valueOf(itemSelectedID), "POI", edit, dialog);
            String whereClauseForRefreshing = POIsContract.POIEntry.COLUMN_CATEGORY_ID + " = " + String.valueOf(categoryID);
            deleteButtonTreatment(itemSelectedID, POI_URI, POI_IDselection, "POI", getWhereClauseCategory("CATEGORY LEVEL"), whereClauseForRefreshing, delete, dialog);
        }
    }

    private void clickedByBasicUserBehaviour(int itemSelectedID){
        try {
            HashMap<String, String> poi = getPOIData(itemSelectedID);//we get the POI data to be shown on LG
            String command = buildCommand(poi); //we build the sentence to send to LG system

            //Network on Main thread forced us to create an AsyncTask
                VisitPoiTask visitPoiTask = new VisitPoiTask(command);
                visitPoiTask.execute();
               // setConnectionWithLiquidGalaxy(command); //we set connection with LG and send the sentence to watch the wished on the LG.
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Error. " + ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }

    /*Other utilities to achieve efectiveness.*/
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

    /*------------TOURS TREATMENT------------*/
    private void showTOURs(){

        initialTourButtonsVisibility();
        showAllTours();
        changeToAllToursOrClassifiedToursTextViewBehaviour();
    }

    private void initialTourButtonsVisibility(){
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
    }

    private void changeToAllToursOrClassifiedToursTextViewBehaviour(){
        seeingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seeingOptions.getText().toString().equals("See elements by category")) { //vull veure-les per categoria
                    toursByCategoriesVisibility();
                    showCategoriesByLevel();

                } else {
                    allToursButtonsVisibility();
                    showAllTours();
                }
            }
        });
    }

    private void allToursButtonsVisibility(){
        seeingOptions.setText("See elements by category");
        backIDs.clear();
        backIDs.add("0");
        if (EDITABLE_TAG.startsWith("ADMIN")) {
            createTourhere.setVisibility(View.GONE);
        }
        poisListViewTittle.setVisibility(View.VISIBLE);
        backIcon.setVisibility(View.GONE);
        backStartIcon.setVisibility(View.GONE);
    }

    private void toursByCategoriesVisibility(){
        poisListViewTittle.setText("Tours");
        seeingOptions.setVisibility(View.VISIBLE);
        seeingOptions.setText("See all elements");
        if (EDITABLE_TAG.startsWith("ADMIN")) {
            createTourhere.setVisibility(View.VISIBLE);
        }
        categoriesListView.setVisibility(View.VISIBLE);
        notify = "TOUR";
    }

    /*Showing all tours*/
    private void showAllTours() {

        seeAllToursOnScreen();
        poisListItemClickedBehaviour();

    }

    private void seeAllToursOnScreen(){
        final Cursor queryCursor;
        if(EDITABLE_TAG.startsWith("USER")) {
            queryCursor = POIsContract.TourEntry.getAllNotHiddenTours(getActivity());
        }else{//ADMIN
            queryCursor = POIsContract.TourEntry.getAllTours(getActivity());
        }
        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("TOUR");

        route.setText("/");

        poisListView.setVisibility(View.VISIBLE);
        categoriesListView.setVisibility(View.GONE);
        poisListView.setAdapter(adapterPOI);
    }

    private void poisListItemClickedBehaviour(){
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

                        tour = new LiquidGalaxyTourView(getActivity());
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

    /*Showing tours by category*/
    private int showToursByCategory(final int categoryID){


        Cursor queryCursor = seeToursByCategoryOnScreen(categoryID);
        tourItemClickedBehaviour(categoryID);



        return queryCursor.getCount();
    }

    private Cursor seeToursByCategoryOnScreen(int categoryID){
        final Cursor queryCursor;
        if (EDITABLE_TAG.startsWith("USER")) {
            queryCursor = POIsContract.TourEntry.getNotHiddenToursByCategory(getActivity(), String.valueOf(categoryID));
        } else {//ADMIN
            queryCursor = POIsContract.TourEntry.getToursByCategory(getActivity(), String.valueOf(categoryID));
        }
        String routeShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), categoryID);
        route.setText(routeShownName);

        adapterPOI = new POIsAdapter(getActivity(), queryCursor, 0);
        adapterPOI.setItemName("TOUR");

        poisListView.setVisibility(View.VISIBLE);
        poisListView.setAdapter(adapterPOI);

        return queryCursor;
    }

    private void tourItemClickedBehaviour(final int categoryID){
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
                        tour = new LiquidGalaxyTourView(getActivity());
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

    //When admin user clicks on one Tour, this dialog appears to offer him/her different options to do with that tour.
    private Dialog getDialogByView(View v){
        // prepare the alert box
        Dialog dialog = new Dialog(getActivity());
        dialog.setTitle("Item Options");
        dialog.setContentView(v);

        return dialog;
    }

    //When user deletes one POI of the tour.
    private int deletePOIsOfTours(String itemSelectedID){
        return POIsContract.TourPOIsEntry.deleteByPoiID(getActivity(), itemSelectedID);
    }

    //When one POI is added inside one Tour when this is being created.
    private void addTourPOIsButtonTreatment(final int itemSelectedID, final String completeName, FloatingActionButton add){
        final TourPOI tourPOI = new TourPOI();
        tourPOI.setPoiName(completeName);
        tourPOI.setPoiID(itemSelectedID);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createORupdate.equals("update")) {
                    UpdateItemFragment.setPOItoTourPOIsList(tourPOI);
                    dialog.dismiss();
                } else {
                    try {
                        CreateItemFragment.setPOItoTourPOIsList(tourPOI);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    //The following six methods are called when user stops the view of one Tour on the LG screens.
    private void stopButtonBehaviour(final LiquidGalaxyTourView tour) {
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert(stopButton, tour);
            }
        });
    }

    private void invalidateOtherClickableElements() {
        tourIsWorking = true;
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


    /*------------OTHER UTILITIES------------*/
    /*The following four methods works when user decides to Edit or Delete one item or when it cancels these just mentioned operations.*/
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
    private void deleteButtonTreatment(final int itemSelectedID, final Uri uri, final String whereClause, final String itemName, final String categoryRefreshListSelection, final String POIorTOURRefreshSelection, FloatingActionButton delete, final Dialog dialog){

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
                    dialog.dismiss();
                }
            }
        });

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

    private class VisitPoiTask extends AsyncTask<Void, Void, String> {

        String command;
        private ProgressDialog dialog;

        public VisitPoiTask(String command) {
            this.command = command;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.viewing_poi));
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
                return LGUtils.setConnectionWithLiquidGalaxy(session, command, getActivity());
            } catch (JSchException e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
                Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String success) {
            super.onPostExecute(success);
            if (success!=null) {
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
            session = LGUtils.getSession(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }
}