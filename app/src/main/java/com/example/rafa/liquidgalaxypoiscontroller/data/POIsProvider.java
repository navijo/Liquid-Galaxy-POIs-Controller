package com.example.rafa.liquidgalaxypoiscontroller.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.CategoryEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.LGTaskEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.POIEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.TourEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.TourPOIsEntry;

public class POIsProvider extends ContentProvider {
    static final int ALL_CATEGORIES = 300;
    static final int ALL_POIS = 100;
    static final int ALL_TOURS = 200;
    static final int ALL_TOUR_POIS = 400;
    static final int ALL_TASKS = 500;
    static final int SINGLE_CATEGORY = 301;
    static final int SINGLE_POI = 101;
    static final int SINGLE_TOUR = 201;
    static final int SINGLE_TOUR_POIS = 401;
    static final int SINGLE_TASK = 501;
    private static final String Category_IDselection = "category._id = ?";
    private static final String POI_IDselection = "poi._id = ?";
    private static final String LGTasks_IDselection = "LG_TASK._id = ?";
    private static final String TourPOIs_IDselection = "Tour_POIs._id = ?";
    private static final String Tour_IDselection = "tour._id = ?";
    private static final UriMatcher sUriMatcher;
    private static POIsDbHelper mOpenHelper;

    static {
        sUriMatcher = buildUriMatcher();
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(-1);
        String authority = POIsContract.CONTENT_AUTHORITY;
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, POIsContract.PATH_POI, ALL_POIS);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, "poi/#", SINGLE_POI);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, POIsContract.PATH_CATEGORY, ALL_CATEGORIES);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, "category/#", SINGLE_CATEGORY);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, POIsContract.PATH_TOUR, ALL_TOURS);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, "tour/#", SINGLE_TOUR);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, POIsContract.PATH_TOUR_POIS, ALL_TOUR_POIS);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, "tourPois/#", SINGLE_TOUR_POIS);

        matcher.addURI(POIsContract.CONTENT_AUTHORITY, POIsContract.PATH_LG_TASK, ALL_TASKS);
        matcher.addURI(POIsContract.CONTENT_AUTHORITY, "lgTask/#", SINGLE_TASK);
        return matcher;
    }

    public static Cursor queryByTaskId(String itemSelectedID) {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT t._id,t.title, t.description, t.script,t.shutdown_script,t.image, t.ip,t.user,t.password,t.url, t.isrunning FROM LG_TASK t WHERE t._id = ?", new String[]{itemSelectedID});
    }

    public static void updateTaskStateByTaskId(String itemSelectedID, boolean isRunning) {
        String sql = "UPDATE LG_TASK SET " + POIsContract.LGTaskEntry.COLUMN_LG_ISRUNNING + "=" + (isRunning ? 1 : 0) + " WHERE _id = ?";

        mOpenHelper.getReadableDatabase().execSQL(sql, new String[]{itemSelectedID});
    }

    public static Cursor getAllLGTasks() {
        String sql = "SELECT t._id,t.title, t.description, t.script,t.shutdown_script,t.image, t.ip,t.user,t.password,t.url, t.isrunning FROM LG_TASK t";
        return mOpenHelper.getReadableDatabase().rawQuery(sql, new String[]{});
    }

    public static Cursor queryByPoiJOINTourPois(String itemSelectedID) {
        String sql = "SELECT t.POI, p.Name, t.POI_Duration FROM poi p INNER JOIN Tour_POIs t ON p._id = t.POI WHERE t.Tour = ? ORDER BY t.POI_Order ASC";
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT t.POI, p.Name, t.POI_Duration FROM poi p INNER JOIN Tour_POIs t ON p._id = t.POI WHERE t.Tour = ? ORDER BY t.POI_Order ASC", new String[]{itemSelectedID});
    }

    public boolean onCreate() {
        mOpenHelper = new POIsDbHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.PATH_POI, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_POI /*101*/:
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.PATH_POI, projection, POI_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_TOURS /*200*/:
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.PATH_TOUR, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_TOUR /*201*/:
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.PATH_TOUR, projection, Tour_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_CATEGORIES /*300*/:
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.PATH_CATEGORY, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_CATEGORY /*301*/:
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.PATH_CATEGORY, projection, Category_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_TOUR_POIS /*400*/:
                cursor = mOpenHelper.getReadableDatabase().query(TourPOIsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_TOUR_POIS /*401*/:
                cursor = mOpenHelper.getReadableDatabase().query(TourPOIsEntry.TABLE_NAME, projection, TourPOIs_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case ALL_TASKS /*500*/:
                cursor = mOpenHelper.getReadableDatabase().query(LGTaskEntry.TABLE_NAME, projection, LGTasks_IDselection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_TASK /*501*/:
                cursor = mOpenHelper.getReadableDatabase().query(LGTaskEntry.TABLE_NAME, projection, LGTasks_IDselection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                return POIEntry.CONTENT_TYPE;
            case SINGLE_POI /*101*/:
                return POIEntry.CONTENT_ITEM_TYPE;
            case ALL_TOURS /*200*/:
                return TourEntry.CONTENT_TYPE;
            case SINGLE_TOUR /*201*/:
                return TourEntry.CONTENT_ITEM_TYPE;
            case ALL_CATEGORIES /*300*/:
                return CategoryEntry.CONTENT_TYPE;
            case SINGLE_CATEGORY /*301*/:
                return CategoryEntry.CONTENT_ITEM_TYPE;
            case ALL_TOUR_POIS /*400*/:
                return TourPOIsEntry.CONTENT_TYPE;
            case SINGLE_TOUR_POIS /*401*/:
                return TourPOIsEntry.CONTENT_ITEM_TYPE;
            case ALL_TASKS /*500*/:
                return LGTaskEntry.CONTENT_TYPE;
            case SINGLE_TASK /*501*/:
                return LGTaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                _id = db.insert(POIsContract.PATH_POI, null, values);
                if (_id > 0) {
                    returnUri = POIEntry.buildPOIUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_TOURS /*200*/:
                _id = db.insert(POIsContract.PATH_TOUR, null, values);
                if (_id > 0) {
                    returnUri = TourEntry.buildTourUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_CATEGORIES /*300*/:
                _id = db.insert(POIsContract.PATH_CATEGORY, null, values);
                if (_id > 0) {
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_TOUR_POIS /*400*/:
                _id = db.insert(TourPOIsEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = TourPOIsEntry.buildTourUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ALL_TASKS /*500*/:
                _id = db.insert(LGTaskEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = LGTaskEntry.buildTourUri(_id);
                    break;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        if (selection == null) {
            selection = "1";
        }
        switch (match) {
            case ALL_POIS /*100*/:
                rowsDeleted = db.delete(POIsContract.PATH_POI, selection, selectionArgs);
                if (rowsDeleted <= 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_TOURS /*200*/:
                rowsDeleted = db.delete(POIsContract.PATH_TOUR, selection, selectionArgs);
                if (rowsDeleted <= 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_CATEGORIES /*300*/:
                rowsDeleted = db.delete(POIsContract.PATH_CATEGORY, selection, selectionArgs);
                if (rowsDeleted <= 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_TOUR_POIS /*400*/:
                rowsDeleted = db.delete(TourPOIsEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted < 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            case ALL_TASKS /*500*/:
                rowsDeleted = db.delete(LGTaskEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted < 0) {
                    throw new SQLException("Failed to delete rows from " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ALL_POIS /*100*/:
                rowsUpdated = db.update(POIsContract.PATH_POI, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_TOURS /*200*/:
                rowsUpdated = db.update(POIsContract.PATH_TOUR, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_CATEGORIES /*300*/:
                rowsUpdated = db.update(POIsContract.PATH_CATEGORY, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_TOUR_POIS /*400*/:
                rowsUpdated = db.update(TourPOIsEntry.TABLE_NAME, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            case ALL_TASKS /*500*/:
                rowsUpdated = db.update(LGTaskEntry.TABLE_NAME, values, selection, selectionArgs);
                if (rowsUpdated < 0) {
                    throw new SQLException("Failed to update rows from " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public void resetDatabase() {
        mOpenHelper.close();
        mOpenHelper = new POIsDbHelper(getContext());
        mOpenHelper.resetDatabase(mOpenHelper.getWritableDatabase());
    }
}
