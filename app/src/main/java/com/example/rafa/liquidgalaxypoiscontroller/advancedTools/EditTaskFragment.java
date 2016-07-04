package com.example.rafa.liquidgalaxypoiscontroller.advancedTools;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;

import java.io.ByteArrayOutputStream;

/**
 * Created by lgwork on 25/05/16.
 */
public class EditTaskFragment extends DialogFragment {

    Handler handler;
    private EditText edit_task_name_input;
    private TextInputLayout edit_task_name;
    private EditText edit_task_description_input;
    private EditText edit_task_script_input;
    private EditText edit_task_shutdown_script_input;
    private EditText edit_task_ip;
    private EditText edit_task_user;
    private EditText edit_task_password;
    private EditText edit_task_browser_URL;
    private Button pickPhotoBtn;
    private Button deletePhotoBtn;
    private ImageView iconview;
    private long taskId;

    public static EditTaskFragment newInstance(long taskId) {
        EditTaskFragment createTask = new EditTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("taskId",taskId);

        createTask.setArguments(bundle);
        return createTask;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
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

        iconview = (ImageView) rootView.findViewById(android.R.id.icon);
        if (currentTask.getImage() != null) {
            iconview.setImageBitmap(BitmapFactory.decodeByteArray(currentTask.getImage(), 0, currentTask.getImage().length));
        }
        deletePhotoBtn = (Button) rootView.findViewById(R.id.deletePhoto);
        deletePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iconview.setImageDrawable(null);
            }
        });

        pickPhotoBtn = (Button) rootView.findViewById(R.id.pickPhoto);
        pickPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 0);
            }
        });

        edit_task_shutdown_script_input = (EditText) rootView.findViewById(R.id.edit_task_shutdown_script_input);
        edit_task_shutdown_script_input.setText(currentTask.getShutdownScript());

        edit_task_ip = (EditText) rootView.findViewById(R.id.edit_task_ip_input);
        edit_task_ip.setText(currentTask.getIp());

        edit_task_user = (EditText) rootView.findViewById(R.id.edit_task_user_input);
        edit_task_user.setText(currentTask.getUser());

        edit_task_password = (EditText) rootView.findViewById(R.id.edit_task_password_input);
        edit_task_password.setText(currentTask.getPassword());

        edit_task_browser_URL = (EditText) rootView.findViewById(R.id.edit_task_url_openBrowser_input);
        edit_task_browser_URL.setText(currentTask.getBrowserUrl());

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
                    if (iconview.getDrawable() != null) {
                        Bitmap bitmap = ((BitmapDrawable) iconview.getDrawable()).getBitmap();
                        newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IMAGE, getBitmapAsByteArray(bitmap));
                    } else {
                        newValues.putNull(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IMAGE);
                    }

                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SHUTDOWNSCRIPT, edit_task_shutdown_script_input.getText().toString());
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IP, edit_task_ip.getText().toString());
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_USER, edit_task_user.getText().toString());
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_PASSWORD, edit_task_password.getText().toString());
                    newValues.put(POIsContract.LGTaskEntry.COLUMN_LG_BROWSER_URL, edit_task_browser_URL.getText().toString());

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            // Get the cursor
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgPath = cursor.getString(columnIndex);

            iconview.setImageBitmap(BitmapFactory.decodeFile(imgPath));
        }
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
        Cursor taskCursor = POIsContract.LGTaskEntry.getTaskById(String.valueOf(taskId));
        LGTask lgTask = new LGTask();
        if (taskCursor.moveToNext()) {
            lgTask.setId(taskCursor.getLong(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_ID)));
            lgTask.setTitle(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_TITLE)));
            lgTask.setDescription(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_DESC)));
            lgTask.setScript(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SCRIPT)));
            lgTask.setImage(taskCursor.getBlob(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IMAGE)));
            lgTask.setShutdownScript(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SHUTDOWNSCRIPT)));
            lgTask.setIp(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IP)));
            lgTask.setUser(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_USER)));
            lgTask.setPassword(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_TASK_PASSWORD)));
            lgTask.setBrowserUrl(taskCursor.getString(taskCursor.getColumnIndex(POIsContract.LGTaskEntry.COLUMN_LG_BROWSER_URL)));
        }

        return lgTask;
    }

}
