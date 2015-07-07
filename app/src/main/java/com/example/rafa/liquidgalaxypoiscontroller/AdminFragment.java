package com.example.rafa.liquidgalaxypoiscontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AdminFragment extends Fragment {

    public AdminFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);
        final ViewHolder viewHolder = new ViewHolder(rootView);

        managementOfPoisToursAndCategories(viewHolder);
        setLogOutButtonBehaviour(viewHolder);
        setNewItemHereButtonBehaviour(viewHolder);
        setNewItemButtonBehaviour(viewHolder);//Creation of a new item

        return rootView;
    }

    private void managementOfPoisToursAndCategories(final ViewHolder viewHolder) {
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
    private void setLogOutButtonBehaviour(ViewHolder viewHolder) {
        viewHolder.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
            }
        });
    }
    private void setNewItemHereButtonBehaviour(ViewHolder viewHolder){

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
    private void setNewItemButtonBehaviour(ViewHolder viewHolder){

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

    public static class ViewHolder {
        public Button createPOI;
        public Button createCategory;
        public Button createTour;
        public Button createPOIhere;
        public Button createCategoryhere;
        public Button createTourhere;

        public Button poisManagement;
        public Button toursManagement;
        public Button categoriesManagement;

        public Button logout;

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
        }
    }
}