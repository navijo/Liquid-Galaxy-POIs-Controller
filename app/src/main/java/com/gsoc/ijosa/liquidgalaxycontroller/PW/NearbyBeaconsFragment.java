/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gsoc.ijosa.liquidgalaxycontroller.PW;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gsoc.ijosa.liquidgalaxycontroller.ImportTourDialogFragment;
import com.gsoc.ijosa.liquidgalaxycontroller.PW.collection.PhysicalWebCollection;
import com.gsoc.ijosa.liquidgalaxycontroller.PW.collection.PwPair;
import com.gsoc.ijosa.liquidgalaxycontroller.PW.collection.PwsResult;
import com.gsoc.ijosa.liquidgalaxycontroller.PW.model.POI;
import com.gsoc.ijosa.liquidgalaxycontroller.R;
import com.gsoc.ijosa.liquidgalaxycontroller.beans.Tour;
import com.gsoc.ijosa.liquidgalaxycontroller.data.POIsContract;
import com.gsoc.ijosa.liquidgalaxycontroller.utils.CustomXmlPullParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class shows the ui list for all
 * detected nearby beacons.
 * It also listens for tap events
 * on items within the list.
 * Tapped list items then launch
 * the browser and point that browser
 * to the given list items url.
 */
public class NearbyBeaconsFragment extends ListFragment implements UrlDeviceDiscoveryService.UrlDeviceDiscoveryListener,
        SwipeRefreshWidget.OnRefreshListener {

    public static final int TOURDATAPICKER_FRAGMENT = 2;
    public static final String TAG = "NearbyBeaconsFragment";
    private static final long FIRST_SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(2);
    private static final long SECOND_SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(5);
    private static final long THIRD_SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(10);
    public static int REQUEST_ENABLE_BLUETOOTH = 1;
    String requestedFileUrl;
    String queriesString = "";
    private List<String> mGroupIdQueue;
    private PhysicalWebCollection mPwCollection = null;
    private TextView mScanningAnimationTextView;
    private AnimationDrawable mScanningAnimationDrawable;
    private Handler mHandler;
    private NearbyBeaconsAdapter mNearbyDeviceAdapter;
    private SwipeRefreshWidget mSwipeRefreshWidget;
    private boolean mDebugViewEnabled = false;
    private boolean mSecondScanComplete;

    private LinearLayout arrowDownLayout;

    private View rootView;

    // The display of gathered urls happens as follows
    // 0. Begin scan
    // 1. Sort and show all urls (mFirstScanTimeout)
    // 2. Sort and show all new urls beneath the first set (mSecondScanTimeout)
    // 3. Show each new url at bottom of list as it comes in
    // 4. Stop scanning (mThirdScanTimeout)
    // Run when the FIRST_SCAN_MILLIS has elapsed.
    private Runnable mFirstScanTimeout = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running first scan timeout");
            if (!mGroupIdQueue.isEmpty()) {
                emptyGroupIdQueue();
                showListView();
            }
        }
    };

    // Run when the SECOND_SCAN_MILLIS has elapsed.
    private Runnable mSecondScanTimeout = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running second scan timeout");
            emptyGroupIdQueue();
            showListView();
            mSecondScanComplete = true;
        }
    };
    private AdapterView.OnItemLongClickListener mAdapterViewItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
                    mDebugViewEnabled = !mDebugViewEnabled;
                    mNearbyDeviceAdapter.notifyDataSetChanged();
                    return true;
                }
            };
    private DiscoveryServiceConnection mDiscoveryServiceConnection = new DiscoveryServiceConnection();
    // Run when the THIRD_SCAN_MILLIS has elapsed.
    private Runnable mThirdScanTimeout = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running third scan timeout");
            mDiscoveryServiceConnection.disconnect();
        }
    };

    public static NearbyBeaconsFragment newInstance() {
        return new NearbyBeaconsFragment();
    }

    private void initialize(View rootView) {


        mGroupIdQueue = new ArrayList<>();
        mHandler = new Handler();

        mSwipeRefreshWidget = (SwipeRefreshWidget) rootView.findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.swipe_refresh_widget_first_color,
                R.color.swipe_refresh_widget_second_color);
        mSwipeRefreshWidget.setOnRefreshListener(this);

        // getActivity().getActionBar().setTitle(R.string.title_nearby_beacons);
        mNearbyDeviceAdapter = new NearbyBeaconsAdapter();
        setListAdapter(mNearbyDeviceAdapter);
        //Get the top drawable
        mScanningAnimationTextView = (TextView) rootView.findViewById(android.R.id.empty);
        mScanningAnimationDrawable =
                (AnimationDrawable) mScanningAnimationTextView.getCompoundDrawables()[1];
        ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        listView.setOnItemLongClickListener(mAdapterViewItemLongClickListener);

        arrowDownLayout = (LinearLayout) rootView.findViewById(R.id.arrow_down);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                initialize(rootView);
            }
        } else if (requestCode == TOURDATAPICKER_FRAGMENT) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                int createdTourId = bundle.getInt("createdTourId");

                ImportAsTourTask importAsTourTask = new ImportAsTourTask(requestedFileUrl, createdTourId);
                importAsTourTask.execute();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = layoutInflater.inflate(R.layout.fragment_nearby_beacons, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setVisibility(View.INVISIBLE);
        mDiscoveryServiceConnection.connect(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDiscoveryServiceConnection.disconnect();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // If we are scanning
        if (mScanningAnimationDrawable.isRunning()) {
            // Don't respond to touch events
            return;
        }
        // Get the url for the given item
        PwPair pwPair = mNearbyDeviceAdapter.getItem(position);
        Intent intent = Utils.createNavigateToUrlIntent(pwPair.getPwsResult());
        startActivity(intent);
    }

    @Override
    public void onUrlDeviceDiscoveryUpdate() {
        for (PwPair pwPair : mPwCollection.getGroupedPwPairsSortedByRank()) {
            String groupId = Utils.getGroupId(pwPair.getPwsResult());
            Log.d(TAG, "groupid to add " + groupId);
            if (mNearbyDeviceAdapter.containsGroupId(groupId)) {
                mNearbyDeviceAdapter.updateItem(pwPair);
            } else if (!mGroupIdQueue.contains(groupId)) {
                mGroupIdQueue.add(groupId);
                if (mSecondScanComplete) {
                    // If we've already waited for the second scan timeout, go ahead and put the item in the
                    // listview.
                    emptyGroupIdQueue();
                }
            }
        }
        safeNotifyChange();
    }

    private void stopScanningDisplay() {
        // Cancel the scan timeout callback if still active or else it may fire later.
        mHandler.removeCallbacks(mFirstScanTimeout);
        mHandler.removeCallbacks(mSecondScanTimeout);
        mHandler.removeCallbacks(mThirdScanTimeout);

        // Change the display appropriately
        mSwipeRefreshWidget.setRefreshing(false);
        mScanningAnimationDrawable.stop();
    }

    private void startScanningDisplay(long scanStartTime, boolean hasResults) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //Bluetooth not available
            Toast.makeText(getActivity(), getResources().getString(R.string.bluetoothNotAvailable), Toast.LENGTH_LONG);
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            // Start the scanning animation only if we don't haven't already been scanning
            // for long enough
            Log.d(TAG, "startScanningDisplay " + scanStartTime + " " + hasResults);
            long elapsedMillis = new Date().getTime() - scanStartTime;
            if (arrowDownLayout != null) {
                arrowDownLayout.setVisibility(View.VISIBLE);
            }
            if (elapsedMillis < FIRST_SCAN_TIME_MILLIS
                    || (elapsedMillis < SECOND_SCAN_TIME_MILLIS && !hasResults)) {
                mScanningAnimationTextView.setAlpha(1f);
                mScanningAnimationDrawable.start();
                if (getListView() != null) {
                    getListView().setVisibility(View.INVISIBLE);
                }
            } else {
                showListView();
            }

            // Schedule the timeouts
            // We delay at least 50 milliseconds to give the discovery service a chance to
            // give us cached results.
            mSecondScanComplete = false;
            long firstDelay = Math.max(FIRST_SCAN_TIME_MILLIS - elapsedMillis, 50);
            long secondDelay = Math.max(SECOND_SCAN_TIME_MILLIS - elapsedMillis, 50);
            long thirdDelay = Math.max(THIRD_SCAN_TIME_MILLIS - elapsedMillis, 50);
            mHandler.postDelayed(mFirstScanTimeout, firstDelay);
            mHandler.postDelayed(mSecondScanTimeout, secondDelay);
            mHandler.postDelayed(mThirdScanTimeout, thirdDelay);
        }
    }

    @Override
    public void onRefresh() {
        // Clear any stored url data
        mGroupIdQueue.clear();
        mNearbyDeviceAdapter.clear();

        // Reconnect to the service
        mDiscoveryServiceConnection.disconnect();
        mSwipeRefreshWidget.setRefreshing(true);
        mDiscoveryServiceConnection.connect(false);
        arrowDownLayout.setVisibility(View.INVISIBLE);
    }

    private void emptyGroupIdQueue() {
        List<PwPair> pwPairs = new ArrayList<>();
        for (String groupId : mGroupIdQueue) {
            Log.d(TAG, "groupid " + groupId);
            pwPairs.add(Utils.getTopRankedPwPairByGroupId(mPwCollection, groupId));
        }
        if (pwPairs != null) {
            Collections.sort(pwPairs, Collections.reverseOrder());
            for (PwPair pwPair : pwPairs) {
                mNearbyDeviceAdapter.addItem(pwPair);
            }
        }
        mGroupIdQueue.clear();
        safeNotifyChange();
    }

    private void showListView() {
        if (getListView() != null) {
            if (getListView().getVisibility() == View.VISIBLE) {
                return;
            }

            mSwipeRefreshWidget.setRefreshing(false);
            getListView().setAlpha(0f);
            getListView().setVisibility(View.VISIBLE);
            safeNotifyChange();
            ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(getListView(), "alpha", 0f, 1f);
            alphaAnimation.setDuration(400);
            alphaAnimation.setInterpolator(new DecelerateInterpolator());
            alphaAnimation.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mScanningAnimationTextView.setAlpha(0f);
                    mScanningAnimationDrawable.stop();
                    arrowDownLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            alphaAnimation.start();
        }
    }

    /**
     * Notify the view that the underlying data has been changed.
     * <p/>
     * We need to make sure the view is visible because if it's not,
     * the view will become visible when we notify it.
     */
    private void safeNotifyChange() {
        if (getListView().getVisibility() == View.VISIBLE) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mNearbyDeviceAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private String getFileName(HttpURLConnection urlConnection) {
        try {
            String fileName = "";
            String raw = urlConnection.getHeaderField("Content-Disposition");
            // raw = "attachment; filename=abc.jpg"
            if (raw != null && raw.contains("=")) {
                fileName = raw.split("=")[1]; //getting value after '='
                fileName = fileName.split(";")[0];
            } else {
                // fall back to random generated file name?
            }
            return fileName.replaceAll("\"", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "unknowFileName";
        }
    }

    private int getImportingFolder(String fileName) {
        int importedCategoryId = POIsContract.CategoryEntry.getIdByName(getActivity(), "PW Beacon Imported");

        int importedSubCategoryId = POIsContract.CategoryEntry.getIdByName(getActivity(), fileName);
        if (importedSubCategoryId > 0) {
            //It exist a category with the same name
            return importedSubCategoryId;
        } else {
            //We create a category for the new folders

            String shownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), importedCategoryId);

            ContentValues newCategory = new ContentValues();
            newCategory.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, importedCategoryId);
            newCategory.put(POIsContract.CategoryEntry.COLUMN_NAME, fileName);
            newCategory.put(POIsContract.CategoryEntry.COLUMN_HIDE, 0);
            newCategory.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, shownName + fileName.trim() + "/");

            Uri categoryUri = POIsContract.CategoryEntry.createNewCategory(getActivity(), newCategory);
            importedCategoryId = POIsContract.CategoryEntry.getIdByUri(categoryUri);
        }

        return importedCategoryId;
    }

    /**
     * The connection to the service that discovers urls.
     */
    private class DiscoveryServiceConnection implements ServiceConnection {
        private UrlDeviceDiscoveryService mDiscoveryService;
        private boolean mRequestCachedUrlDevices;

        @Override
        public synchronized void onServiceConnected(ComponentName className, IBinder service) {
            // Get the service
            UrlDeviceDiscoveryService.LocalBinder localBinder =
                    (UrlDeviceDiscoveryService.LocalBinder) service;
            mDiscoveryService = localBinder.getServiceInstance();

            // Start the scanning display
            mDiscoveryService.addCallback(NearbyBeaconsFragment.this);
            if (!mRequestCachedUrlDevices) {
                mDiscoveryService.restartScan();
            }
            mPwCollection = mDiscoveryService.getPwCollection();
            startScanningDisplay(mDiscoveryService.getScanStartTime(), mDiscoveryService.hasResults());
        }

        @Override
        public synchronized void onServiceDisconnected(ComponentName className) {
            // onServiceDisconnected gets called when the connection is unintentionally disconnected,
            // which should never happen for us since this is a local service
            mDiscoveryService = null;
        }

        public synchronized void connect(boolean requestCachedUrlDevices) {
            if (mDiscoveryService != null) {
                return;
            }

            mRequestCachedUrlDevices = requestCachedUrlDevices;
            Intent intent = new Intent(getActivity(), UrlDeviceDiscoveryService.class);
            getActivity().startService(intent);
            getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
        }

        public synchronized void disconnect() {
            if (mDiscoveryService == null) {
                return;
            }

            mDiscoveryService.removeCallback(NearbyBeaconsFragment.this);
            mDiscoveryService = null;
            getActivity().unbindService(this);
            stopScanningDisplay();
        }
    }

    // Adapter for holding beacons found through scanning.
    private class NearbyBeaconsAdapter extends BaseAdapter {
        private List<PwPair> mPwPairs;

        NearbyBeaconsAdapter() {
            mPwPairs = new ArrayList<>();
        }

        public void addItem(PwPair pwPair) {
            mPwPairs.add(pwPair);
        }

        public void updateItem(PwPair pwPair) {
            String groupId = Utils.getGroupId(pwPair.getPwsResult());
            for (int i = 0; i < mPwPairs.size(); ++i) {
                if (Utils.getGroupId(mPwPairs.get(i).getPwsResult()).equals(groupId)) {
                    mPwPairs.set(i, pwPair);
                    return;
                }
            }
            throw new RuntimeException("Cannot find PwPair with group " + groupId);
        }

        public boolean containsGroupId(String groupId) {
            for (PwPair pwPair : mPwPairs) {
                if (Utils.getGroupId(pwPair.getPwsResult()).equals(groupId)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getCount() {
            return mPwPairs.size();
        }

        @Override
        public PwPair getItem(int i) {
            return mPwPairs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private void setText(View view, int textViewId, String text) {
            ((TextView) view.findViewById(textViewId)).setText(text);
        }

        private void addButtonImportAsPois(Button btn, final String url) {

            btn.setText(getResources().getString(R.string.import_pois_dialog_title));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestedFileUrl = url;
                    ImportPOISTask importAsPOISTask = new ImportPOISTask(requestedFileUrl);
                    importAsPOISTask.execute();
                }
            });
        }

        private void addButtonImportAsTour(Button btn, final String url) {

            btn.setText(getResources().getString(R.string.importAsTourStr));
            btn.setBackground(getActivity().getResources().getDrawable(R.drawable.button_rounded_grey));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(5, 0, 0, 0);
            btn.setLayoutParams(params);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestedFileUrl = url;
                    ImportTourDialogFragment tourDataFragment = ImportTourDialogFragment.newInstance();
                    tourDataFragment.setTargetFragment(NearbyBeaconsFragment.this, TOURDATAPICKER_FRAGMENT);
                    tourDataFragment.show(getFragmentManager().beginTransaction(), "Tour Data");
                }
            });
        }



        @SuppressLint("InflateParams")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            // Get the list view item for the given position
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_nearby_beacon,
                        viewGroup, false);
            }

            // Display the pwsResult.
            PwPair pwPair = getItem(i);
            PwsResult pwsResult = pwPair.getPwsResult();

            setText(view, R.id.title, pwsResult.getTitle());
            setText(view, R.id.url, pwsResult.getSiteUrl());
            setText(view, R.id.description, pwsResult.getDescription());
            ((ImageView) view.findViewById(R.id.icon)).setImageBitmap(Utils.getBitmapIcon(mPwCollection, pwsResult));

            Button btnImportAsPOIs = (Button) view.findViewById(R.id.btnImportAsPOIS);
            Button btnImportAsTour = (Button) view.findViewById(R.id.btnImportAsTour);

            if (pwsResult.getSiteUrl().contains("drive")) {
                btnImportAsPOIs.setVisibility(View.VISIBLE);
                btnImportAsTour.setVisibility(View.VISIBLE);

                addButtonImportAsPois(btnImportAsPOIs, pwsResult.getSiteUrl());
                addButtonImportAsTour(btnImportAsTour, pwsResult.getSiteUrl());

            } else {
                btnImportAsPOIs.setVisibility(View.INVISIBLE);
                btnImportAsTour.setVisibility(View.INVISIBLE);
            }

            (view.findViewById(R.id.icon)).setVisibility(View.VISIBLE);
            mPwCollection.setPwsEndpoint(Utils.PROD_ENDPOINT);
            UrlShortenerClient.getInstance(getActivity()).setEndpoint(Utils.PROD_ENDPOINT);

            return view;
        }


        public void clear() {
            mPwPairs.clear();
            notifyDataSetChanged();
        }
    }

    private class ImportPOISTask extends AsyncTask<Void, Integer, Boolean> {

        String fileId;
        String downloadUrl = "";
        private ProgressDialog importingDialog;


        ImportPOISTask(String fileUrl) {
            String[] urlSplitted = fileUrl.split("/");
            this.fileId = urlSplitted[5];

            this.downloadUrl = "https://docs.google.com/uc?authuser=0&id=" + this.fileId + "&export=download";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (importingDialog == null) {
                importingDialog = new ProgressDialog(getActivity());
                importingDialog.setMessage(getResources().getString(R.string.importingContents));
                importingDialog.setIndeterminate(false);
                importingDialog.setMax(100);
                importingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                importingDialog.setCancelable(true);
                importingDialog.setCanceledOnTouchOutside(false);
                importingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                importingDialog.show();
            }

        }


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return importPOIS();
            } catch (Exception e) {
                cancel(true);
                return null;
            }
        }


        private boolean importPOIS() throws IOException {
            URL url;
            boolean success= false;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(this.downloadUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                String fileName = getFileName(urlConnection);

                CustomXmlPullParser customXmlPullParser = new CustomXmlPullParser();
                final List<POI> poisList = customXmlPullParser.parse(in, getActivity());

                success = createPOIS(fileName, poisList);

                return success;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
                return success;
            }
        }




        private boolean createPOIS(String fileName, List<POI> poisList) {
            Looper.prepare();

            long total = 0;

            int importedCategoryId = getImportingFolder(fileName);

                for(POI poiImported: poisList){
                    total++;
                    ContentValues poi = getFromImportedPOI(poiImported, importedCategoryId);

                    try {

                        Uri insertedUri = POIsContract.POIEntry.createNewPOI(getActivity(), poi);
                        Toast.makeText(getActivity(), insertedUri.toString(), Toast.LENGTH_SHORT).show();
                        publishProgress((int)(total*100/poisList.size()));
                    }catch (android.database.SQLException e){
                        String poiName = poi.get(POIsContract.POIEntry.COLUMN_COMPLETE_NAME).toString();
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Already exists one POI with the same name: " + poiName + ". Please, change it.", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
         }

        @Override
        protected void onProgressUpdate(Integer... values) {
            importingDialog.setProgress(values[0]);
        }

        private ContentValues getFromImportedPOI(POI poiImported, int earthCategory) {

            ContentValues poi = new ContentValues();

            String name = poiImported.getName();
            String longitude = poiImported.getPoint().getLongitude();
            String latitude = poiImported.getPoint().getLatitude();
            String altitude = "0";
            String heading = "79";
            String tilt = "62";
            String range = "500";
            String altitudeMode = "relativeToSeaFloor";

            poi.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, name);
            poi.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, name);
            poi.put(POIsContract.POIEntry.COLUMN_LONGITUDE, longitude);
            poi.put(POIsContract.POIEntry.COLUMN_LATITUDE, latitude);
            poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE, altitude);
            poi.put(POIsContract.POIEntry.COLUMN_HEADING, heading);
            poi.put(POIsContract.POIEntry.COLUMN_TILT, tilt);
            poi.put(POIsContract.POIEntry.COLUMN_RANGE, range);
            poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, altitudeMode);
            poi.put(POIsContract.POIEntry.COLUMN_HIDE, 0);
            poi.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, earthCategory);

            return poi;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
                if (importingDialog != null) {
                    importingDialog.hide();
                    importingDialog.dismiss();
                }

            if (!success) {
                Toast.makeText(getActivity(), getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ImportAsTourTask extends AsyncTask<Void, Integer, Void> {

        String fileId;
        String downloadUrl = "";
        int tourId;
        private ProgressDialog importingDialog;

        ImportAsTourTask(String fileUrl, int tourId) {
            String[] urlSplitted = fileUrl.split("/");
            this.fileId = urlSplitted[5];
            this.tourId = tourId;
            this.downloadUrl = "https://docs.google.com/uc?authuser=0&id=" + this.fileId + "&export=download";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (importingDialog == null) {
                importingDialog = new ProgressDialog(getActivity());
                importingDialog.setMessage(getResources().getString(R.string.importingContents));
                importingDialog.setIndeterminate(false);
                importingDialog.setMax(100);
                importingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                importingDialog.setCancelable(true);
                importingDialog.setCanceledOnTouchOutside(false);
                importingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                importingDialog.show();
            }
        }


        @Override
        protected Void doInBackground(Void... params) {
            try {
                importAsVisit();
            } catch (Exception e) {
                cancel(true);
                return null;
            }
            return null;
        }


        private void importAsVisit() throws IOException {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(this.downloadUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                String fileName = getFileName(urlConnection);

                CustomXmlPullParser customXmlPullParser = new CustomXmlPullParser();
                List<POI> poisList = customXmlPullParser.parse(in, getActivity());
                createPOISAndAddToTour(poisList, this.tourId, fileName);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        private boolean createPOISAndAddToTour(List<POI> poisList, int tourId, String fileName) {
            Looper.prepare();

            long total = 0;
            Cursor tourCursor = POIsContract.TourEntry.getTourById(getActivity(), tourId);
            Tour tour = null;
            if (tourCursor.moveToFirst()) {
                tour = getTourFromCursor(tourCursor);
            }

            int importedCategoryId = getImportingFolder(fileName);

            for (POI poiImported : poisList) {
                total++;
                ContentValues poi = getFromImportedPOI(poiImported, importedCategoryId);

                try {

                    Uri insertedUri = POIsContract.POIEntry.createNewPOI(getActivity(), poi);
                    int newPoiId = POIsContract.POIEntry.getIdByUri(insertedUri);

                    ContentValues tourPoiValues = new ContentValues();
                    tourPoiValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_ID, newPoiId);
                    tourPoiValues.put(POIsContract.TourPOIsEntry.COLUMN_TOUR_ID, tour != null ? tour.getId() : 0);
                    tourPoiValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_DURATION, tour != null ? tour.getDuration() : 10);
                    tourPoiValues.put(POIsContract.TourPOIsEntry.COLUMN_POI_ORDER, total);

                    POIsContract.TourPOIsEntry.createNewTourPOI(getActivity(), tourPoiValues);

                    publishProgress((int) (total * 100 / poisList.size()));
                } catch (android.database.SQLException e) {
                    String poiName = poi.get(POIsContract.POIEntry.COLUMN_COMPLETE_NAME).toString();
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Already exists one POI with the same name: " + poiName + ". Please, change it.", Toast.LENGTH_LONG).show();
                }
            }
            return true;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            importingDialog.setProgress(values[0]);
        }

        private Tour getTourFromCursor(Cursor tourCursor) {
            Tour tour = new Tour();
            tour.setId(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_ID)));
            tour.setName(tourCursor.getString(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_NAME)));
            tour.setCategoryId(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_CATEGORY_ID)));
            tour.setDuration(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_INTERVAL)));
            tour.setHidden(tourCursor.getInt(tourCursor.getColumnIndex(POIsContract.TourEntry.COLUMN_HIDE)) == 1);

            return tour;
        }


        private ContentValues getFromImportedPOI(POI poiImported, int earthCategory) {

            ContentValues poi = new ContentValues();

            String name = poiImported.getName();
            String longitude = poiImported.getPoint().getLongitude();
            String latitude = poiImported.getPoint().getLatitude();
            String altitude = "0";
            String heading = "79";
            String tilt = "62";
            String range = "500";
            String altitudeMode = "relativeToSeaFloor";

            poi.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, name);
            poi.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, name);
            poi.put(POIsContract.POIEntry.COLUMN_LONGITUDE, longitude);
            poi.put(POIsContract.POIEntry.COLUMN_LATITUDE, latitude);
            poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE, altitude);
            poi.put(POIsContract.POIEntry.COLUMN_HEADING, heading);
            poi.put(POIsContract.POIEntry.COLUMN_TILT, tilt);
            poi.put(POIsContract.POIEntry.COLUMN_RANGE, range);
            poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, altitudeMode);
            poi.put(POIsContract.POIEntry.COLUMN_HIDE, 0);
            poi.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, earthCategory);

            return poi;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
            if (importingDialog != null) {
                importingDialog.dismiss();
            }
        }
    }

}

