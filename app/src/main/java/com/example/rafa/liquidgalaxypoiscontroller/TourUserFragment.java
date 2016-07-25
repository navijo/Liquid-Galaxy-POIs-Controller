package com.example.rafa.liquidgalaxypoiscontroller;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.rafa.liquidgalaxypoiscontroller.beans.Tour;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.utils.ToursGridViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lgwork on 22/07/16.
 */
public class TourUserFragment extends Fragment {

    View rootView;
    GridView toursGridView;
    private ListView categoriesListView;
    private TextView categorySelectorTitle, currentCategoryText;
    private Button show_all;
    private ImageView backIcon, backStartIcon;
    private CategoriesAdapter adapter;
    private ArrayList<String> backIDs = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tours, container, false);

        toursGridView = (GridView) rootView.findViewById(R.id.TOURSgridview);

        categoriesListView = (ListView) rootView.findViewById(R.id.categories_listview);
        backIcon = (ImageView) rootView.findViewById(R.id.back_icon);
        backStartIcon = (ImageView) rootView.findViewById(R.id.back_start_icon);
        categorySelectorTitle = (TextView) rootView.findViewById(R.id.current_category);
        currentCategoryText = (TextView) rootView.findViewById(R.id.viewing_category_text);
        show_all = (Button) rootView.findViewById(R.id.show_all);

        backStartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIDs.clear();
                showAllBaseCategories();
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backIDs.size() > 1) {
                    backIDs.remove(0);
                    Cursor queryCursor = getCategoriesCursor();
                    showCategoriesOnScreen(queryCursor);
                } else {
                    showAllBaseCategories();
                }
            }
        });

        show_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (show_all.getText().toString().equalsIgnoreCase(getResources().getString(R.string.show_all))) {
                    showAllTours();
                    categoriesListView.setVisibility(View.INVISIBLE);
                    backIcon.setVisibility(View.GONE);
                    backStartIcon.setVisibility(View.GONE);
                } else {
                    show_all.setText(getResources().getString(R.string.show_all));
                    categoriesListView.setVisibility(View.VISIBLE);
                    backIcon.setVisibility(View.VISIBLE);
                    backStartIcon.setVisibility(View.VISIBLE);
                    showToursByCat();
                }
            }
        });

        return rootView;
    }

    private void showToursByCat() {
        final List<Tour> toursList;
        if (backIDs.size() > 0) {
            toursList = getTours(Integer.parseInt(backIDs.get(0)));
        } else {
            toursList = getNotCategorizedTours();
            categorySelectorTitle.setVisibility(View.GONE);
            currentCategoryText.setVisibility(View.GONE);
        }
        toursGridView.setAdapter(new ToursGridViewAdapter(toursList, getActivity(), getActivity()));

        Cursor queryCursor = getCategoriesCursor();
        showCategoriesOnScreen(queryCursor);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        showToursByCat();
    }

    private void showCategoriesOnScreen(Cursor queryCursor) {
        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);

        if (queryCursor.getCount() > 0) {
            categoriesListView.setAdapter(adapter);

            if (backIDs.size() > 0) {
                String currentCategoryName = POIsContract.CategoryEntry.getNameById(getActivity(), Integer.parseInt(backIDs.get(0)));
                categorySelectorTitle.setVisibility(View.VISIBLE);
                currentCategoryText.setVisibility(View.VISIBLE);
                categorySelectorTitle.setText(currentCategoryName);
            }

            categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);//gets the category selected
                    if (cursor != null) {
                        int itemSelectedID = cursor.getInt(0);
                        backIDs.add(0, String.valueOf(itemSelectedID));

                        showToursByCategory();
                    }
                }
            });
        } else {
            categoriesListView.setAdapter(null);
        }
    }

    private void showAllTours() {
        final List<Tour> toursList = getAllTours();
        toursGridView.setAdapter(new ToursGridViewAdapter(toursList, getActivity(), getActivity()));
        show_all.setText(getResources().getString(R.string.showByCategories));
        categorySelectorTitle.setVisibility(View.GONE);
        currentCategoryText.setVisibility(View.GONE);
    }
    
    private List<Tour> getTours(int categoryId) {

        List<Tour> lTours = new ArrayList<>();

        try (Cursor allVisibleToursCursor = POIsContract.TourEntry.getNotHiddenToursByCategory(getActivity(), String.valueOf(categoryId))) {

            while (allVisibleToursCursor.moveToNext()) {

                int tourId = allVisibleToursCursor.getInt(0);

                Tour tourEntry = getTourData(tourId);
                lTours.add(tourEntry);
            }
        }
        return lTours;
    }

    private List<Tour> getAllTours() {

        List<Tour> lTours = new ArrayList<>();

        try (Cursor allVisibleToursCursor = POIsContract.TourEntry.getAllNotHiddenTours(getActivity())) {

            while (allVisibleToursCursor.moveToNext()) {

                int tourId = allVisibleToursCursor.getInt(0);

                Tour tourEntry = getTourData(tourId);
                lTours.add(tourEntry);
            }
        }
        return lTours;
    }

    private List<Tour> getNotCategorizedTours() {
        List<Tour> lTours = new ArrayList<>();

        try (Cursor allVisibleToursCursor = POIsContract.TourEntry.getNotCategorizedTours(getActivity())) {

            while (allVisibleToursCursor.moveToNext()) {

                int tourId = allVisibleToursCursor.getInt(0);

                Tour tourEntry = getTourData(tourId);
                lTours.add(tourEntry);
            }
        }
        return lTours;
    }

    private void showAllBaseCategories() {
        Cursor queryCursor = POIsContract.CategoryEntry.getRootCategories(getActivity());
        showCategoriesOnScreen(queryCursor);
        showNotCategorizedTours();
    }

    private void showNotCategorizedTours() {

        categorySelectorTitle.setVisibility(View.GONE);
        currentCategoryText.setVisibility(View.GONE);

        final List<Tour> toursList = getNotCategorizedTours();
        if (toursList != null) {
            toursGridView.setAdapter(new ToursGridViewAdapter(toursList, getActivity(), getActivity()));
        }
    }

    private void showToursByCategory() {

        Cursor queryCursor = getCategoriesCursor();
        showCategoriesOnScreen(queryCursor);

        categorySelectorTitle.setVisibility(View.VISIBLE);
        currentCategoryText.setVisibility(View.VISIBLE);

        String currentCategoryName = POIsContract.CategoryEntry.getNameById(getActivity(), Integer.parseInt(backIDs.get(0)));

        categorySelectorTitle.setText(currentCategoryName);


        final List<Tour> toursList = getTours(Integer.parseInt(backIDs.get(0)));
        if (toursList != null) {
            toursGridView.setAdapter(new ToursGridViewAdapter(toursList, getActivity(), getActivity()));
        }
    }

    private Cursor getCategoriesCursor() {
        if (backIDs.size() > 0) {
            return POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), backIDs.get(0));
        } else {
            return POIsContract.CategoryEntry.getRootCategories(getActivity());
        }
    }

    private Tour getTourData(int tourId) {
        Tour tourEntry = new Tour();
        Cursor tourCursor = POIsContract.TourEntry.getTourById(getActivity(), tourId);

        if (tourCursor.moveToNext()) {
            tourEntry.setId(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_ID)));
            tourEntry.setName(tourCursor.getString(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_NAME)));
            tourEntry.setDuration(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_INTERVAL)));
            tourEntry.setCategoryId(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_CATEGORY_ID)));
            tourEntry.setHidden(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_HIDE)) == 1);
        }
        tourCursor.close();
        return tourEntry;
    }
}
