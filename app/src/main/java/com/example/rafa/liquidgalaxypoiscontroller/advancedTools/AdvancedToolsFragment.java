package com.example.rafa.liquidgalaxypoiscontroller.advancedTools;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.LGTaskEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsProvider;
import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lgwork on 30/06/16.
 */
public class AdvancedToolsFragment extends Fragment{

    private RecyclerView rv = null;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton fab;

    public static AdvancedToolsFragment newInstance(){
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

    private void populateUI() {
       Cursor allTasksCursor =  POIsProvider.getAllLGTasks();
        List<LGTask> taksList = new ArrayList<LGTask>();
        try {
            while (allTasksCursor.moveToNext()) {
                int taskId = allTasksCursor.getInt(0);
                taksList.add(getTaskData(taskId));
            }
            fillAdapter(taksList);
        } finally {
            allTasksCursor.close();
            refreshLayout.setRefreshing(false);
        }
    }

    private LGTask getTaskData(int taskId) {
        Cursor taskCursor = LGTaskEntry.getTaskById(this.getActivity(),String.valueOf(taskId));
        LGTask lgTask = new LGTask();
        if (taskCursor.moveToNext()) {
            lgTask.setId(taskCursor.getLong(taskCursor.getColumnIndex(LGTaskEntry.COLUMN_LG_TASK_ID)));
            lgTask.setTitle(taskCursor.getString(taskCursor.getColumnIndex(LGTaskEntry.COLUMN_LG_TASK_TITLE)));
            lgTask.setDescription(taskCursor.getString(taskCursor.getColumnIndex(LGTaskEntry.COLUMN_LG_TASK_DESC)));
            lgTask.setScript(taskCursor.getString(taskCursor.getColumnIndex(LGTaskEntry.COLUMN_LG_TASK_SCRIPT)));
        }

        return lgTask;
    }

    void showCreateDialog() {
        CreateTaskFragment newFragment = CreateTaskFragment.newInstance();
        newFragment.show(getFragmentManager(), "createDialog");
    }

    void showEditDialog(long taskId) {
        EditTaskFragment newFragment = EditTaskFragment.newInstance(taskId);
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
                taskHolder.taskScript = lgTask.getScript();
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
//                POISListFragment poisListFragment = POISListFragment.newInstance(document);
//                fragmentStackManager.loadFragment(poisListFragment, R.id.main_frame);
            }
        });
    }


    private class LGTaskHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView taskTitle;
        TextView taskDescription;
        ImageView filePhoto;

        long id;
        String taskScript;


        LGTask task;


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
//                            POISListFragment poisListFragment = POISListFragment.newInstance(document);
//                            fragmentStackManager.loadFragment(poisListFragment, R.id.main_frame);
                            break;
                        case R.id.launchTask:
//                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                            BluetoothUtils.ensureBluetoothIsEnabled(getActivity(), bluetoothAdapter);
//
//                            BeaconConfigFragment beaconConfigFragment = BeaconConfigFragment.newInstance(fileLink);
//                            fragmentStackManager.loadFragment(beaconConfigFragment, R.id.main_frame);
                            break;

                        case R.id.deleteTask:
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle(getResources().getString(R.string.are_you_sure));

                            alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
//                                    deleteTask = new MakeDeleteTask(mCredential, fileResourceId);
//                                    deleteTask.execute();
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
//            menu.setHeaderTitle(getResources().getString(R.string.context_menu_title));
//
//            MenuItem deleteItem = menu.add(0, v.getId(), 2, R.string.context_menu_delete);
//            deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//
//                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//                    alert.setTitle(getResources().getString(R.string.are_you_sure));
//
//                    alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            deleteTask = new MakeDeleteTask(mCredential, fileResourceId);
//                            deleteTask.execute();
//                        }
//                    });
//
//                    alert.setNegativeButton(getResources().getString(R.string.no),
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int whichButton) {
//                                }
//                            });
//
//                    alert.show();
//                    return true;
//                }
//            });
//
//
//            MenuItem shareitem = menu.add(0, v.getId(), 0, R.string.context_menu_share);
//            shareitem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//
//                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                    BluetoothUtils.ensureBluetoothIsEnabled(getActivity(), bluetoothAdapter);
//
//                    BeaconConfigFragment beaconConfigFragment = BeaconConfigFragment.newInstance(fileLink);
//                    fragmentStackManager.loadFragment(beaconConfigFragment, R.id.main_frame);
//
//                    return true;
//                }
//            });
//
//
//            MenuItem editItem = menu.add(0, v.getId(), 1, R.string.context_menu_edit);
//            editItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    RenameDocumentFragment renameDocumentFragment = RenameDocumentFragment.newInstance(fileResourceId, documentTitle.getText().toString(),
//                            documentDescription.getText().toString());
//                    fragmentStackManager.loadFragment(renameDocumentFragment, R.id.main_frame);
//                    return true;
//                }
//            });
        }
    }
}
