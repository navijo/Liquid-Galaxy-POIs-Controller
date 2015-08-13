package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {

    private String filePath = "";
    private ViewHolder viewHolder;
    private String EDITABLE = "";

    public AdminFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);
        viewHolder = new ViewHolder(rootView);

        EDITABLE = getArguments().getString("EDITABLE");

        managementOfPoisToursAndCategories(rootView);
        setLogOutButtonBehaviour();
        setImportPOIsButtonBehaviour();
        setNewItemHereButtonBehaviour();
        setNewItemButtonBehaviour();//Creation of a new item

        return rootView;
    }

    //Manage the buttons visibility depending on the button the user clicks.

    private void managementOfPoisToursAndCategories(View rootView) {

        POISFragment fragment = new POISFragment();
        Bundle args = new Bundle();

        if(EDITABLE.equals("POIS")) {

            args.putString("EDITABLE", "ADMIN/POIS");
            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, fragment).commit();
            if (viewHolder.createPOI.getVisibility() == View.GONE) {
                viewHolder.createTour.setVisibility(View.GONE);
                viewHolder.createCategory.setVisibility(View.GONE);
                viewHolder.createPOI.setVisibility(View.VISIBLE);
                viewHolder.createTourhere.setVisibility(View.GONE);
                viewHolder.createCategoryhere.setVisibility(View.GONE);
                viewHolder.createPOIhere.setVisibility(View.GONE);
//                POISFragment.setAdminView(rootView);
            }
        }

        else if(EDITABLE.equals("TOURS")) {

            args.putString("EDITABLE", "ADMIN/TOURS");
            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, fragment).commit();
            if (viewHolder.createTour.getVisibility() == View.GONE) {
                viewHolder.createPOI.setVisibility(View.GONE);
                viewHolder.createCategory.setVisibility(View.GONE);
                viewHolder.createTour.setVisibility(View.VISIBLE);
                viewHolder.createPOIhere.setVisibility(View.GONE);
                viewHolder.createCategoryhere.setVisibility(View.GONE);
                viewHolder.createTourhere.setVisibility(View.GONE);
//                POISFragment.setAdminView(rootView);
            }
        }

        else if(EDITABLE.equals("CATEGORIES")) {

            args.putString("EDITABLE", "ADMIN/CATEGORIES");
            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, fragment).commit();
            if (viewHolder.createCategory.getVisibility() == View.GONE) {
                viewHolder.createPOI.setVisibility(View.GONE);
                viewHolder.createTour.setVisibility(View.GONE);
                viewHolder.createCategory.setVisibility(View.VISIBLE);
                viewHolder.createPOIhere.setVisibility(View.GONE);
                viewHolder.createTourhere.setVisibility(View.GONE);
                viewHolder.createCategoryhere.setVisibility(View.GONE);
//                POISFragment.setAdminView(rootView);
            }
        }
    }
    //Behaviour for leaving Administration section.
    private void setLogOutButtonBehaviour() {
        viewHolder.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To leave out the Administration section
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
            }
        });
    }

    //When user decide to create an item inside the category he/she is watching in the screen.
    private void setNewItemHereButtonBehaviour(){
        //Depending on the button clicked, the user will see an interface or other, depending on the type of the item
        viewHolder.createCategoryhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "CATEGORY/HERE");
                startActivity(createPoiIntent);
            }
        });

        viewHolder.createPOIhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "POI/HERE");
                startActivity(createPoiIntent);
            }
        });

        viewHolder.createTourhere.setOnClickListener(new View.OnClickListener() {
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

        viewHolder.createCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createCategoryIntent = new Intent(getActivity(), CreateItemActivity.class);
                createCategoryIntent.putExtra("CREATION_TYPE", "CATEGORY");
                startActivity(createCategoryIntent);
            }
        });

        viewHolder.createPOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPoiIntent = new Intent(getActivity(), CreateItemActivity.class);
                createPoiIntent.putExtra("CREATION_TYPE", "POI");
                startActivity(createPoiIntent);
            }
        });

        viewHolder.createTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent createTourIntent = new Intent(getActivity(), CreateItemActivity.class);
                createTourIntent.putExtra("CREATION_TYPE", "TOUR");
                startActivity(createTourIntent);
            }
        });
    }

    //All the following methods are the different steps to import a file with POIs.
    private void setImportPOIsButtonBehaviour() {
        viewHolder.importPois.setOnClickListener(new View.OnClickListener() {
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
                    readPOI(poisList, line, categoryID);
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

    public static class ViewHolder {
        private Button createPOI;
        private Button createCategory;
        private Button createTour;
        private Button createPOIhere;
        private Button createCategoryhere;
        private Button createTourhere;

        private Button poisManagement;
        private Button toursManagement;
        private Button categoriesManagement;

        private Button logout;
        private Button importPois;

        public ViewHolder(View rootView) {

            poisManagement = (Button) rootView.findViewById(R.id.pois_management);
            toursManagement = (Button) rootView.findViewById(R.id.tours_management);
            categoriesManagement = (Button) rootView.findViewById(R.id.categories_management);
            createPOI = (Button) rootView.findViewById(R.id.new_poi);
            createCategory = (Button) rootView.findViewById(R.id.new_category);
            createTour = (Button) rootView.findViewById(R.id.new_tour);
            createPOIhere = (Button) rootView.findViewById(R.id.new_poi_here);
            createCategoryhere = (Button) rootView.findViewById(R.id.new_category_here);
            createTourhere = (Button) rootView.findViewById(R.id.new_tour_here);
            logout = (Button) rootView.findViewById(R.id.admin_logout);
            importPois = (Button) rootView.findViewById(R.id.import_pois);
        }
    }

}

//    private void managementOfPoisToursAndCategories() {
//        viewHolder.poisManagement.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/POIS").commit();
//                if(viewHolder.createPOI.getVisibility() == View.GONE){
//                    viewHolder.createTour.setVisibility(View.GONE);
//                    viewHolder.createCategory.setVisibility(View.GONE);
//                    viewHolder.createPOI.setVisibility(View.VISIBLE);
//                    viewHolder.createTourhere.setVisibility(View.GONE);
//                    viewHolder.createCategoryhere.setVisibility(View.GONE);
//                    viewHolder.createPOIhere.setVisibility(View.GONE);
//                    POISFragment.setAdminView(getView());
//                }
//            }
//        });
//
//        viewHolder.toursManagement.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/TOURS").commit();
//                if(viewHolder.createTour.getVisibility() == View.GONE){
//                    viewHolder.createPOI.setVisibility(View.GONE);
//                    viewHolder.createCategory.setVisibility(View.GONE);
//                    viewHolder.createTour.setVisibility(View.VISIBLE);
//                    viewHolder.createPOIhere.setVisibility(View.GONE);
//                    viewHolder.createCategoryhere.setVisibility(View.GONE);
//                    viewHolder.createTourhere.setVisibility(View.GONE);
//                    POISFragment.setAdminView(getView());
//                }
//            }
//        });
//
//        viewHolder.categoriesManagement.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/CATEGORIES").commit();
//                if(viewHolder.createCategory.getVisibility() == View.GONE){
//                    viewHolder.createPOI.setVisibility(View.GONE);
//                    viewHolder.createTour.setVisibility(View.GONE);
//                    viewHolder.createCategory.setVisibility(View.VISIBLE);
//                    viewHolder.createPOIhere.setVisibility(View.GONE);
//                    viewHolder.createTourhere.setVisibility(View.GONE);
//                    viewHolder.createCategoryhere.setVisibility(View.GONE);
//                    POISFragment.setAdminView(getView());
//                }
//            }
//        });
//    }