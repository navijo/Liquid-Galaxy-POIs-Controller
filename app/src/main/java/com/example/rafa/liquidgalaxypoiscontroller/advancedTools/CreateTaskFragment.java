package com.example.rafa.liquidgalaxypoiscontroller.advancedTools;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;

/**
 * Created by lgwork on 25/05/16.
 */
public class CreateTaskFragment extends DialogFragment {

    private EditText new_task_name_input;
    private TextInputLayout new_task_name;
    private EditText new_task_description_input;
    private EditText new_task_script_input;

    public static CreateTaskFragment newInstance() {
        CreateTaskFragment createTask = new CreateTaskFragment();
        Bundle bundle = new Bundle();
        createTask.setArguments(bundle);
        return createTask;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_new_task, container, false);
//        fragmentStackManager = FragmentStackManager.getInstance(getActivity());
        getDialog().setTitle(R.string.add_new_task);
         Button saveTask = (Button) rootView.findViewById(R.id.btn_save_task);
        Button btnCancel = (Button) rootView.findViewById(R.id.btn_cancel_add_task);


        new_task_name_input = (EditText) rootView.findViewById(R.id.new_task_name_input);
        new_task_name = (TextInputLayout) rootView.findViewById(R.id.new_task_name);

        new_task_description_input = (EditText) rootView.findViewById(R.id.new_task_description_input);
        new_task_script_input = (EditText) rootView.findViewById(R.id.new_task_script_input);

        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources res = getActivity().getResources();

                if (new_task_name_input.getText().toString().length() == 0) {
                    new_task_name.setError(res.getString(R.string.empty_name_error));
                } else {
                    new_task_name.setErrorEnabled(false);

                    ContentValues newTask = new ContentValues();
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_TITLE, new_task_name_input.getText().toString());
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_DESC, new_task_description_input.getText().toString());
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SCRIPT,new_task_script_input.getText().toString());

                    Uri insertedUri = POIsContract.LGTaskEntry.createNewTask(getActivity(), newTask);
                    Toast.makeText(getActivity(),getResources().getString(R.string.task_added_ok), Toast.LENGTH_LONG).show();
                    getDialog().dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
