package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Josa on 18/07/16.
 */
public class ImportTourDialogFragment extends DialogFragment {

    EditText tourName;
    Switch visibility;
    EditText duration;
    Spinner category;
    Button saveBtn;
    Button cancelBtn;
    TextInputLayout tourNameInputLayout;
    TextInputLayout poisIntervalInputLayout;

    public static ImportTourDialogFragment newInstance() {
        ImportTourDialogFragment importTour = new ImportTourDialogFragment();
        Bundle bundle = new Bundle();
        importTour.setArguments(bundle);
        return importTour;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.import_tour, container, false);

        getDialog().setTitle(R.string.tourValues);

        tourName = (EditText) rootView.findViewById(R.id.tour_name);
        visibility = (Switch) rootView.findViewById(R.id.switchButtonHide);
        duration = (EditText) rootView.findViewById(R.id.pois_interval);
        category = (Spinner) rootView.findViewById(R.id.categoryID_spinner);

        saveBtn = (Button) rootView.findViewById(R.id.saveTourBtn);

        tourNameInputLayout = (TextInputLayout) rootView.findViewById(R.id.tourNameinputLayout);
        poisIntervalInputLayout = (TextInputLayout) rootView.findViewById(R.id.poisIntervalInputLayout);

        fillCategorySpinner(category);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tourName.getText().toString().length() == 0) {
                    tourNameInputLayout.setError(getResources().getString(R.string.empty_name_error));
                } else if (duration.getText().toString().length() == 0) {
                    poisIntervalInputLayout.setError(getResources().getString(R.string.empty_duration_error));
                } else {
                    tourNameInputLayout.setErrorEnabled(false);
                    poisIntervalInputLayout.setErrorEnabled(false);

                    ContentValues newTourValues = new ContentValues();
                    newTourValues.put(POIsContract.TourEntry.COLUMN_NAME, tourName.getText().toString());
                    newTourValues.put(POIsContract.TourEntry.COLUMN_HIDE, visibility.isChecked() ? 0 : 1);
                    newTourValues.put(POIsContract.TourEntry.COLUMN_INTERVAL, duration.getText().toString());
                    newTourValues.put(POIsContract.TourEntry.COLUMN_CATEGORY_ID, category.getSelectedItemId());

                    Uri insertedUri = POIsContract.TourEntry.createNewTOUR(getActivity(), newTourValues);
                    int newTourID = POIsContract.TourEntry.getIdByUri(insertedUri);

                    dismiss();

                    Intent backValues = new Intent();
                    backValues.putExtra("createdTourId", newTourID);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, backValues);
                }
            }
        });

        cancelBtn = (Button) rootView.findViewById(R.id.cancelTourBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }

    private void fillCategorySpinner(Spinner spinner) {

        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.noRouteText));

        //We get all the categories IDs and ShownNames
        Cursor queryCursor = POIsContract.CategoryEntry.getIDsAndShownNamesOfAllCategories(getActivity());

        while (queryCursor.moveToNext()) {
            list.add(queryCursor.getString(1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        queryCursor.close();
    }

}
