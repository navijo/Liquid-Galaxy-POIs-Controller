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

package com.example.rafa.liquidgalaxypoiscontroller.PW;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.PW.collection.PhysicalWebCollection;
import com.example.rafa.liquidgalaxypoiscontroller.PW.collection.PwPair;
import com.example.rafa.liquidgalaxypoiscontroller.PW.collection.PwsResult;
import com.example.rafa.liquidgalaxypoiscontroller.PW.model.POI;
import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.utils.CustomXmlPullParser;

import org.apache.http.conn.params.ConnManagerParams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    public static final String TAG = "NearbyBeaconsFragment";
    private static final long FIRST_SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(2);
    private static final long SECOND_SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(2);
    private static final long THIRD_SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(2);
    private List<String> mGroupIdQueue;
    private PhysicalWebCollection mPwCollection = null;
    private TextView mScanningAnimationTextView;
    private AnimationDrawable mScanningAnimationDrawable;
    private Handler mHandler;
    private NearbyBeaconsAdapter mNearbyDeviceAdapter;
    private SwipeRefreshWidget mSwipeRefreshWidget;
    private boolean mDebugViewEnabled = false;
    private boolean mSecondScanComplete;


    String requestedFileUrl;
    String queriesString = "";

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

    // Run when the THIRD_SCAN_MILLIS has elapsed.
    private Runnable mThirdScanTimeout = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running third scan timeout");
            mDiscoveryServiceConnection.disconnect();
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

    private DiscoveryServiceConnection mDiscoveryServiceConnection = new DiscoveryServiceConnection();

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

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = layoutInflater.inflate(R.layout.fragment_nearby_beacons, container, false);
        initialize(rootView);
        return rootView;
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
        // Start the scanning animation only if we don't haven't already been scanning
        // for long enough
        Log.d(TAG, "startScanningDisplay " + scanStartTime + " " + hasResults);
        long elapsedMillis = new Date().getTime() - scanStartTime;
        if (elapsedMillis < FIRST_SCAN_TIME_MILLIS
                || (elapsedMillis < SECOND_SCAN_TIME_MILLIS && !hasResults)) {
            mScanningAnimationTextView.setAlpha(1f);
            mScanningAnimationDrawable.start();
            getListView().setVisibility(View.INVISIBLE);
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

    @Override
    public void onRefresh() {
        // Clear any stored url data
        mGroupIdQueue.clear();
        mNearbyDeviceAdapter.clear();

        // Reconnect to the service
        mDiscoveryServiceConnection.disconnect();
        mSwipeRefreshWidget.setRefreshing(true);
        mDiscoveryServiceConnection.connect(false);
    }

    private void emptyGroupIdQueue() {
        List<PwPair> pwPairs = new ArrayList<>();
        for (String groupId : mGroupIdQueue) {
            Log.d(TAG, "groupid " + groupId);
            pwPairs.add(Utils.getTopRankedPwPairByGroupId(mPwCollection, groupId));
        }
        Collections.sort(pwPairs, Collections.reverseOrder());
        for (PwPair pwPair : pwPairs) {
            mNearbyDeviceAdapter.addItem(pwPair);
        }
        mGroupIdQueue.clear();
        safeNotifyChange();
    }

    private void showListView() {
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

        private void addButton(Button btn, final String url) {

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

            if (pwsResult.getSiteUrl().contains("drive")) {
                btnImportAsPOIs.setVisibility(View.VISIBLE);

                addButton(btnImportAsPOIs, pwsResult.getSiteUrl());

            } else {
                btnImportAsPOIs.setVisibility(View.INVISIBLE);
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
        private ProgressDialog dialog;

        public ImportPOISTask(String fileUrl) {
            String[] urlSplitted = fileUrl.split("/");
            this.fileId = urlSplitted[5];

            this.downloadUrl = "https://docs.google.com/uc?authuser=0&id=" + this.fileId + "&export=download";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.importingContents));
                dialog.setIndeterminate(false);
                dialog.setMax(100);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
                return importAsVisit();
            } catch (Exception e) {
                cancel(true);
                return null;
            }
        }


        private boolean importAsVisit() throws IOException {
            URL url = null;
            boolean success= false;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(this.downloadUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());


                CustomXmlPullParser customXmlPullParser = new CustomXmlPullParser();
                final List<POI> poisList = customXmlPullParser.parse(in, getActivity());

                //createTourGalaxyDocument(poisList);

                success = createPOIS(poisList);

                return success;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return success;
        }


        private boolean createPOIS(List<POI> poisList) {
            Looper.prepare();

            long total = 0;

            int earthCategory =  POIsContract.CategoryEntry.getIdByShownName(getActivity(), "EARTH" + "/");
                for(POI poiImported: poisList){
                    total++;
                    ContentValues poi = getFromImportedPOI(poiImported,earthCategory);

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
            dialog.setProgress(values[0]);
        }

        private ContentValues getFromImportedPOI(POI poiImported, int earthCategory) {

            ContentValues poi = new ContentValues();

            String name = poiImported.getName();
            String visited_place = name;
            String longitude = poiImported.getPoint().getLongitude();
            String latitude = poiImported.getPoint().getLatitude();
            String altitude = "0";
            String heading = "79";
            String tilt = "62";
            String range = "339";
            String altitudeMode = "relativeToSeaFloor";

            poi.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, name);
            poi.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, visited_place);
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
            if (success) {
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
            }
        }


        private void createTourGalaxyDocument(List<POI> poisList) {
            StringBuilder categoryStrB = new StringBuilder("importTest");
            StringBuilder finalQueriesStrB = new StringBuilder();

            for (POI poi : poisList) {
                finalQueriesStrB.append(categoryStrB);
                finalQueriesStrB.append("@");
                finalQueriesStrB.append(poi.getName());
                finalQueriesStrB.append("@");
                finalQueriesStrB.append("flytoview=<LookAt><longitude>");
                finalQueriesStrB.append(poi.getPoint().getLongitude());
                finalQueriesStrB.append("</longitude><latitude>");
                finalQueriesStrB.append(poi.getPoint().getLatitude());
                finalQueriesStrB.append("</latitude>");
                finalQueriesStrB.append("<altitude>0</altitude>");
                finalQueriesStrB.append("<heading>78.8295920519042</heading>");
                finalQueriesStrB.append("<tilt>61.91707350294211</tilt>");
                finalQueriesStrB.append("<range>338.9856138972227</range>");
                finalQueriesStrB.append("<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode></LookAt>");
                finalQueriesStrB.append("\n");
            }
            queriesString = finalQueriesStrB.toString();
        }

    }


    private class createPoisTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return null;
        }
    }

}

