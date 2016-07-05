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
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by lgwork on 25/05/16.
 */
public class CreateTaskFragment extends DialogFragment {

    private static int ACTION_UPLOAD_LOGO = 1;
    private static int ACTION_UPLOAD_EXEC_SCRIPT = 2;
    private static int ACTION_UPLOAD_SHUTDOWN_SCRIPT = 3;


    Handler handler;
    private EditText new_task_name_input;
    private TextInputLayout new_task_name;
    private EditText new_task_description_input;
    private EditText new_task_script_input;
    private EditText new_task_shutdown_script_input;
    private EditText new_task_ip;
    private EditText new_task_user;
    private EditText new_task_password;
    private EditText new_task_browser_URL;
    private Button pickPhotoBtn;
    private ImageView iconview;

    private Button uploadExecutionScript;
    private Button uploadShutdownScript;

    public static CreateTaskFragment newInstance() {
        CreateTaskFragment createTask = new CreateTaskFragment();
        Bundle bundle = new Bundle();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_UPLOAD_LOGO) {
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
        } else if (requestCode == ACTION_UPLOAD_EXEC_SCRIPT) {
            if (data != null) {

                Uri uri = data.getData();
                if (uri != null) {
                    String path = uri.toString();
                    String filePath;
                    //We get the file path by executing one of the following methods, depending the explorer the user uses.
                    if (path.toLowerCase().startsWith("file://")) {
                        filePath = (new File(URI.create(path))).getAbsolutePath();
                    } else {
                        filePath = pathTreatment(data.getData().getPath(), Environment.getExternalStorageDirectory().getAbsolutePath());
                    }
                    String readedFile = readFile(filePath);
                    new_task_script_input.setText(readedFile);
                }

            }

        } else if (requestCode == ACTION_UPLOAD_SHUTDOWN_SCRIPT) {
            if (data != null) {

                Uri uri = data.getData();
                if (uri != null) {
                    String path = uri.toString();
                    String filePath;
                    //We get the file path by executing one of the following methods, depending the explorer the user uses.
                    if (path.toLowerCase().startsWith("file://")) {
                        filePath = (new File(URI.create(path))).getAbsolutePath();
                    } else {
                        filePath = pathTreatment(data.getData().getPath(), Environment.getExternalStorageDirectory().getAbsolutePath());
                    }
                    String readedFile = readFile(filePath);
                    new_task_shutdown_script_input.setText(readedFile);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_new_task, container, false);

        getDialog().setTitle(R.string.add_new_task);
        Button saveTask = (Button) rootView.findViewById(R.id.btn_add_task);
        Button btnCancel = (Button) rootView.findViewById(R.id.btn_cancel_add_task);

        iconview = (ImageView) rootView.findViewById(android.R.id.icon);
        pickPhotoBtn = (Button) rootView.findViewById(R.id.pickPhoto);
        pickPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, ACTION_UPLOAD_LOGO);
            }
        });


        new_task_shutdown_script_input = (EditText) rootView.findViewById(R.id.new_task_shutdown_script_input);
        new_task_ip = (EditText) rootView.findViewById(R.id.new_task_ip_input);
        new_task_user = (EditText) rootView.findViewById(R.id.new_task_user_input);
        new_task_password = (EditText) rootView.findViewById(R.id.new_task_password_input);
        new_task_browser_URL = (EditText) rootView.findViewById(R.id.new_task_url_openBrowser_input);

        new_task_name_input = (EditText) rootView.findViewById(R.id.new_task_name_input);
        new_task_name = (TextInputLayout) rootView.findViewById(R.id.new_task_name);

        new_task_description_input = (EditText) rootView.findViewById(R.id.new_task_description_input);
        new_task_script_input = (EditText) rootView.findViewById(R.id.new_task_script_input);

        uploadExecutionScript = (Button) rootView.findViewById(R.id.uploadExecutionScript);
        uploadExecutionScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(intent, ACTION_UPLOAD_EXEC_SCRIPT);
            }
        });

        uploadShutdownScript = (Button) rootView.findViewById(R.id.uploadShutDownScript);
        uploadShutdownScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(intent, ACTION_UPLOAD_SHUTDOWN_SCRIPT);
            }
        });


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
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SCRIPT, new_task_script_input.getText().toString());
                    if (iconview.getDrawable() != null) {
                        Bitmap bitmap = ((BitmapDrawable) iconview.getDrawable()).getBitmap();
                        newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IMAGE, getBitmapAsByteArray(bitmap));
                    }

                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_SHUTDOWNSCRIPT, new_task_shutdown_script_input.getText().toString());
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_IP, new_task_ip.getText().toString());
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_USER, new_task_user.getText().toString());
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_TASK_PASSWORD, new_task_password.getText().toString());
                    newTask.put(POIsContract.LGTaskEntry.COLUMN_LG_BROWSER_URL, new_task_browser_URL.getText().toString());

                    Uri insertedUri = POIsContract.LGTaskEntry.createNewTask(getActivity(), newTask);
                    Toast.makeText(getActivity(), getResources().getString(R.string.task_added_ok), Toast.LENGTH_LONG).show();
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

    private String pathTreatment(String path, String absolutePath) {
        int start = 0;
        String firstPathFolder = "";
        if (path.contains(":")) {
            start = path.indexOf(":") + 1;
            path = path.substring(start);
        }

        if (path.startsWith("/")) {
            firstPathFolder = path.split("/")[1];
        } else {
            firstPathFolder = path.split("/")[0];
        }
        String[] absoluteFolders = absolutePath.split("/");
        String lastAbsoluteFolder = absoluteFolders[absoluteFolders.length - 1];

        if (firstPathFolder.equals(lastAbsoluteFolder)) {
            if (path.startsWith("/")) {
                path = path.substring(firstPathFolder.length() + 2);
            } else {
                path = path.substring(firstPathFolder.length() + 1);
            }
        }

        if (!absolutePath.endsWith("/")) {
            absolutePath = absolutePath + "/";
        }
        return absolutePath + path;
    }

    private String readFile(String filePath) {
        StringBuilder readedStringBuilder = new StringBuilder();
        File file = new File(filePath);
        if (file.exists()) {

            try {
                FileInputStream inputStream = null;
                inputStream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = "";

                while ((line = br.readLine()) != null) {
                    if (!line.equals("") && line != null) {
                        readedStringBuilder.append(line);
                        readedStringBuilder.append("\n");
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "File couldn't be opened. Try to open it with a different file explorer, for example 'Root Explorer' once.", Toast.LENGTH_LONG).show();
        }
        return readedStringBuilder.toString();
    }

}
