package com.example.rafa.liquidgalaxypoiscontroller.advancedTools;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.LGTaskEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsProvider;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Ivan Josa on 30/06/16.
 */
public class AdvancedToolsFragment extends Fragment {

    ImageButton documentListHelpBtn;
    private RecyclerView rv = null;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton fab;

    public static AdvancedToolsFragment newInstance() {
        Bundle args = new Bundle();

        AdvancedToolsFragment fragment = new AdvancedToolsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.advanced_tools_list, container, false);

        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        fab = (FloatingActionButton) rootView.findViewById(R.id.add_app);


        documentListHelpBtn = (ImageButton) rootView.findViewById(R.id.documentListHelp);

        documentListHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // custom dialog
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.help_task_list_dialog);
                dialog.setTitle(getResources().getString(R.string.taskListHelpTitle));

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        populateUI();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        populateUI();
                    }
                }
        );

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDialog();
            }
        });
    }

    public void populateUI() {
        Cursor allTasksCursor = POIsProvider.getAllLGTasks();
        List<LGTask> taksList = new ArrayList<LGTask>();
        try {
            while (allTasksCursor.moveToNext()) {
                int taskId = allTasksCursor.getInt(0);
                if (taskId == 1) {
                    //If the task is the LG task, we need to add the  browser URL parameter
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String hostname = prefs.getString("HostName", "172.26.17.21");
                    LGTask task = getTaskData(taskId);
                    if (task.getBrowserUrl() == null) {
                        POIsContract.LGTaskEntry.updateTaskURlById(String.valueOf(taskId), hostname + ":81/php-interface/index.php");
                    }
                }
                taksList.add(getTaskData(taskId));
            }
            fillAdapter(taksList);
        } finally {
            allTasksCursor.close();
            refreshLayout.setRefreshing(false);
        }
    }

    private LGTask getTaskData(int taskId) {
        Cursor taskCursor = LGTaskEntry.getTaskById(String.valueOf(taskId));
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
            lgTask.setRunning(taskCursor.getInt(taskCursor.getColumnIndex(LGTaskEntry.COLUMN_LG_ISRUNNING)) == 1);
        }
        return lgTask;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        populateUI();
    }

    void showCreateDialog() {
        CreateTaskFragment newFragment = CreateTaskFragment.newInstance();
        newFragment.setHandler(new DialogFragmentDismissHandler());
        newFragment.show(getFragmentManager(), "createDialog");
    }

    void showEditDialog(long taskId) {
        EditTaskFragment newFragment = EditTaskFragment.newInstance(taskId);
        newFragment.setHandler(new DialogFragmentDismissHandler());
        newFragment.show(getFragmentManager(), "editDialog");
    }

    private void fillAdapter(final List<LGTask> tasks) {
        ParallaxRecyclerAdapter<LGTask> parallaxRecyclerAdapter = new ParallaxRecyclerAdapter<LGTask>(tasks) {
            @Override

            public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, ParallaxRecyclerAdapter<LGTask> parallaxRecyclerAdapter, int i) {
                LGTask lgTask = parallaxRecyclerAdapter.getData().get(i);

                LGTaskHolder taskHolder = (LGTaskHolder) viewHolder;
                taskHolder.id = lgTask.getId();
                taskHolder.taskTitle.setText(lgTask.getTitle());
                taskHolder.taskDescription.setText(lgTask.getDescription());
                taskHolder.script = lgTask.getScript();
                if (lgTask.getImage() != null) {
                    taskHolder.filePhoto.setImageBitmap(BitmapFactory.decodeByteArray(lgTask.getImage(), 0, lgTask.getImage().length));
                }

                taskHolder.shutdownScript = lgTask.getShutdownScript();
                taskHolder.browserUrl = lgTask.getBrowserUrl();
                taskHolder.ip = lgTask.getIp();
                taskHolder.user = lgTask.getUser();
                taskHolder.password = lgTask.getPassword();

                Toolbar toolbarCard = (Toolbar) viewHolder.itemView.findViewById(R.id.taskToolbar);
                //FIXMe: Review
                if (lgTask.isRunning()) {
                    //We hide the play button
                    toolbarCard.getMenu().getItem(0).setVisible(false);
                } else {
                    //we hide the stop button
                    toolbarCard.getMenu().getItem(1).setVisible(false);
                }
            }


            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
                super.onBindViewHolder(viewHolder, i);
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup viewGroup, final ParallaxRecyclerAdapter<LGTask> parallaxRecyclerAdapter, int i) {
                return new LGTaskHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.advanced_tools_list_item_card, viewGroup, false));
            }

            @Override
            public int getItemCountImpl(ParallaxRecyclerAdapter<LGTask> parallaxRecyclerAdapter) {
                return tasks.size();
            }
        };

        parallaxRecyclerAdapter.setParallaxHeader(getActivity().getLayoutInflater().inflate(R.layout.advanced_tools_list_header_layout, rv, false), rv);


        rv.setAdapter(parallaxRecyclerAdapter);

        //On click on recycler view item
        parallaxRecyclerAdapter.setOnClickEvent(new ParallaxRecyclerAdapter.OnClickEvent() {
            @Override
            public void onClick(View view, int i) {
                LGTask task = tasks.get(i);
                SendCommandTask sendCommandTask = new SendCommandTask(task.getScript(), task.getIp(), task.getUser(), task.getPassword(), task.getBrowserUrl(), false, task.getId());
                sendCommandTask.execute();
            }
        });
    }

    //This private class handles when the dialog dismiss and refresh the ui with the changes
    private class DialogFragmentDismissHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            populateUI();
        }
    }

    private class LGTaskHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView taskTitle;
        TextView taskDescription;
        ImageView filePhoto;

        long id;
        String script;
        String shutdownScript;
        String browserUrl;

        String ip;
        String user;
        String password;

        public LGTaskHolder(View itemView) {
            super(itemView);
            taskTitle = (TextView) itemView.findViewById(R.id.task_title);
            taskDescription = (TextView) itemView.findViewById(R.id.task_description);
            filePhoto = (ImageView) itemView.findViewById(R.id.file_photo);

            itemView.setOnCreateContextMenuListener(this);


            Toolbar toolbarCard = (Toolbar) itemView.findViewById(R.id.taskToolbar);
            toolbarCard.inflateMenu(R.menu.menu_lgtask_cardview);
            toolbarCard.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.editTask:
                            showEditDialog(id);
                            break;
                        case R.id.launchTask:
                            SendCommandTask sendCommandTask = new SendCommandTask(script, ip, user, password, browserUrl, false, id);
                            sendCommandTask.execute();
                            break;
                        case R.id.stopTask:
                            SendCommandTask sendStopCommandTask = new SendCommandTask(shutdownScript, ip, user, password, browserUrl, true, id);
                            sendStopCommandTask.execute();
                            break;
                        case R.id.deleteTask:
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle(getResources().getString(R.string.are_you_sure));

                            alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    POIsContract.LGTaskEntry.deleteByTaskID(getActivity(), String.valueOf(id));
                                    populateUI();
                                }
                            });

                            alert.setNegativeButton(getResources().getString(R.string.no),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    });
                            alert.show();
                            break;
                    }
                    return true;
                }
            });
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }

    private class SendCommandTask extends AsyncTask<Void, Void, Boolean> {

        long taskId;
        String command;
        String ip;
        String user;
        String password;
        String browserUrl;
        boolean isStop;

        private ProgressDialog dialog;

        public SendCommandTask(String command, String ip, String user, String password, String browserUrl, boolean isStop, long taskId) {
            this.command = command;
            this.ip = ip;
            this.user = user;
            this.password = password;
            this.browserUrl = browserUrl;
            this.isStop = isStop;
            this.taskId = taskId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.sending_command));
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
        protected Boolean doInBackground(Void... params) {
            try {
                return sendCommandToLG(command, ip, user, password);
                // return sendCommandToLGShell(command, ip, user, password);
            } catch (JSchException e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }

                return null;
            } catch (IOException e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success && !this.isStop) {

                if (!getTaskData(Integer.parseInt(String.valueOf(taskId))).getShutdownScript().equalsIgnoreCase("")) {
                    POIsContract.LGTaskEntry.updateTaskStateById(String.valueOf(taskId), true);
                }

                if (browserUrl != null && !browserUrl.equals("")) {
                    if (!browserUrl.startsWith("http://") && !browserUrl.startsWith("https://"))
                        browserUrl = "http://" + browserUrl;

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl));
                    startActivity(browserIntent);
                }
                populateUI();

            } else if (success) {
                POIsContract.LGTaskEntry.updateTaskStateById(String.valueOf(taskId), false);
                populateUI();
            }
            if (dialog != null) {
                dialog.hide();
                dialog.dismiss();
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            Toast.makeText(getActivity(), getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
        }


        private Boolean sendCommandToLG(String command, String pIp, String pUser, String pPassword) throws Exception {


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String user = (pUser != null && !pUser.equals("")) ? pUser : prefs.getString("User", "lg");
            String password = (pPassword != null && !pPassword.equals("")) ? pPassword : prefs.getString("Password", "lqgalaxy");
            String hostname = (pIp != null && !pIp.equals("")) ? pIp : prefs.getString("HostName", "172.26.17.21");
            int port = Integer.parseInt(prefs.getString("Port", "22"));

            JSch jsch = new JSch();

            Session session = jsch.getSession(user, hostname, port);
            session.setPassword(password);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect();

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            channelExec.setOutputStream(baos);

            //command = "export GSOC=\\\"/home/lg/Desktop/lglab\\\"  && "+command;

            //We need to redirect the output to a log file
            channelExec.setCommand(command + " >> " + "LGControllerLog.txt");

            channelExec.connect();

            channelExec.disconnect();
            baos.toString();
            return true;
        }

    }

}
