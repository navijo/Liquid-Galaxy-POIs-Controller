package com.example.rafa.liquidgalaxypoiscontroller.advancedTools;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
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
public class EditTaskFragment extends DialogFragment {

    private EditText edit_task_name_input;
    private TextInputLayout edit_task_name;
    private EditText edit_task_description_input;
    private EditText edit_task_script_input;

    private long taskId;

    Handler handler;



    public static EditTaskFragment newInstance(long taskId) {
        EditTaskFragment createTask = new EditTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("taskId",taskId);

        createTask.setArguments(bundle);
        return createTask;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        handler.sendEmptyMessage(0);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.edit_task, container, false);
        this.taskId = getArguments().getLong("taskId");

        LGTask currentTask = getTaskData(Integer.parseInt(String.valueOf(taskId)));

        getDialog().setTitle(R.string.edit_task);
        Button saveTask = (Button) rootView.findViewById(R.id.btn_save_task);
        Button btnCancel = (Button) rootView.findViewById(R.id.btn_cancel_edit_task);


        edit_task_name_input = (EditText) rootView.findViewById(R.id.edit_task_name_input);
        edit_task_name = (TextInputLayout) rootView.findViewById(R.id.edit_task_name);

        edit_task_name_input.setText(currentTask.getTitle());

        edit_task_description_input = (EditText) rootView.findViewById(R.id.edit_task_description_input);
        edit_task_script_input = (EditText) rootView.findViewById(R.id.edit_task_script_input);

        edit_task_description_input.setText(currentTask.getDescription());
        edit_task_script_input.setText(currentTask.getScript());

        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources res = getActivity().getResources();

                if (edit_task_name_input.getText().toString().length() == 0) {
                    edit_task_name.setError(res.getString(R.string.empty_name_error));
                } else {
                    edit_task_name.setErrorEnabled(false);

                    ContentValues newValues = new ContentValues();
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_TITLE, edit_task_name_input.getText().toString());
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_DESC, edit_task_description_input.getText().toString());
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SCRIPT,edit_task_script_input.getText().toString());

                    int result = POIsContract.LGTaskEntry.updateByTaskId(getActivity(), newValues, String.valueOf(taskId));
                    Toast.makeText(getActivity(),getResources().getString(R.string.task_updated_ok), Toast.LENGTH_LONG).show();
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

    private LGTask getTaskData(int taskId) {
        Cursor taskCursor = POIsContract.LGTaskEntry.getTaskById(this.getActivity(),String.valueOf(taskId));
        LGTask lgTask = new LGTask();
        if (taskCursor.moveToNext()) {
            lgTask.setId(taskCursor.getLong(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_ID)));
            lgTask.setTitle(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_TITLE)));
            lgTask.setDescription(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_DESC)));
            lgTask.setScript(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SCRIPT)));
        }

        return lgTask;
    }

}
