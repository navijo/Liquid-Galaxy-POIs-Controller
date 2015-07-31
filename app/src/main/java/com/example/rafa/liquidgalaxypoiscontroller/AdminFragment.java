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
import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {

    private String filePath = "";
    private ViewHolder viewHolder;

    public AdminFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);
        viewHolder = new ViewHolder(rootView);

        managementOfPoisToursAndCategories();
        setLogOutButtonBehaviour();
        setImportPOIsButtonBehaviour();
        setNewItemHereButtonBehaviour();
        setNewItemButtonBehaviour();//Creation of a new item

        return rootView;
    }

    private void managementOfPoisToursAndCategories() {
        viewHolder.poisManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/POIS").commit();
                if(viewHolder.createPOI.getVisibility() == View.GONE){
                    viewHolder.createTour.setVisibility(View.GONE);
                    viewHolder.createCategory.setVisibility(View.GONE);
                    viewHolder.createPOI.setVisibility(View.VISIBLE);
                    viewHolder.createTourhere.setVisibility(View.GONE);
                    viewHolder.createCategoryhere.setVisibility(View.GONE);
                    viewHolder.createPOIhere.setVisibility(View.GONE);
                    POISFragment.setAdminView(getView());
                }
            }
        });

        viewHolder.toursManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/TOURS").commit();
                if(viewHolder.createTour.getVisibility() == View.GONE){
                    viewHolder.createPOI.setVisibility(View.GONE);
                    viewHolder.createCategory.setVisibility(View.GONE);
                    viewHolder.createTour.setVisibility(View.VISIBLE);
                    viewHolder.createPOIhere.setVisibility(View.GONE);
                    viewHolder.createCategoryhere.setVisibility(View.GONE);
                    viewHolder.createTourhere.setVisibility(View.GONE);
                    POISFragment.setAdminView(getView());
                }
            }
        });

        viewHolder.categoriesManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.management_button_container, new POISFragment(), "ADMIN/CATEGORIES").commit();
                if(viewHolder.createCategory.getVisibility() == View.GONE){
                    viewHolder.createPOI.setVisibility(View.GONE);
                    viewHolder.createTour.setVisibility(View.GONE);
                    viewHolder.createCategory.setVisibility(View.VISIBLE);
                    viewHolder.createPOIhere.setVisibility(View.GONE);
                    viewHolder.createTourhere.setVisibility(View.GONE);
                    viewHolder.createCategoryhere.setVisibility(View.GONE);
                    POISFragment.setAdminView(getView());
                }
            }
        });
    }
    private void setImportPOIsButtonBehaviour() {
        viewHolder.importPois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFileToImport();
            }
        });
    }
    private void setLogOutButtonBehaviour() {
        viewHolder.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
            }
        });
    }
    private void setNewItemHereButtonBehaviour(){

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){

                filePath = pathTreatment(data.getData().getPath(), Environment.getExternalStorageDirectory().getAbsolutePath());
//                filePath = pathTreatment(data.getData().getPath());
                String category = getFileName();
                int categoryID = createCategory(category);
                if(categoryID != 0){
                    List<ContentValues> poisToImport = readFile(categoryID);
                    createPOis(poisToImport);
                }
            }
        }
    }
    private List<ContentValues> readFile(int categoryID) {
        List<ContentValues> poisList = new ArrayList<ContentValues>();
//        try {
//            //FileInputStream fis = new FileInputStream (new File(filePath));
//            FileInputStream fis = getActivity().openFileInput(filePath);
//              InputStream is = getClass().getResourceAsStream(filePath);
//            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//            String line = "";
//
//            while ((line = br.readLine()) != null) {
//                viewHolder.logout.setText(line);
//                readPOI(poisList, line, categoryID);
//            }
//            br.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Toast.makeText(getActivity(), "File", Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(getActivity(), "IO", Toast.LENGTH_LONG).show();
//        }

//        filePath = "/mnt/sdcard/Download/Taiwan.txt";
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
                    readPOI(poisList, line, categoryID);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return poisList;
    }
    private void readPOI(List<ContentValues> poisList, String line, int categoryID){

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
    }
    private String getFileName(){
        int startIndex = filePath.lastIndexOf("/") + 1;
        return filePath.substring(startIndex, filePath.length() - 4);
    }
    private void selectFileToImport(){
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
                Toast.makeText(getActivity(), "Already exists one POI with the same name: " + poiName + ". Please, change it.", Toast.LENGTH_SHORT).show();
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