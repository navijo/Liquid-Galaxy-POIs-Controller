package com.gsoc.ijosa.liquidgalaxycontroller.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;

/**
 * Created by RAFA on 18/05/2015.
 */
public class POIsContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.gsoc.ijosa.liquidgalaxycontroller";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.gsoc.ijosa.liquidgalaxycontroller.app/poi/ is a valid path for
    // looking at POI data. content://com.gsoc.ijosa.liquidgalaxycontroller.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_POI = "poi";
    public static final String PATH_TOUR = "tour";
    public static final String PATH_CATEGORY = "category";
    public static final String PATH_TOUR_POIS = "tourPois";
    public static final String PATH_LG_TASK = "lgTask";

    public static int delete(FragmentActivity fragmentActivity, Uri uri, String whereClause, String[] whereClauseArgs) {
        return fragmentActivity.getContentResolver().delete(uri, whereClause, whereClauseArgs);
    }

    public static final class POIEntry implements BaseColumns {

        //CONTENT_URI = content://com.gsoc.ijosa.liquidgalaxycontroller/poi/
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POI).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POI;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POI;

        // Table name
        public static final String TABLE_NAME = "poi";

        //Table columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_COMPLETE_NAME = "Name";
        public static final String COLUMN_VISITED_PLACE_NAME = "Visited_Place";
        public static final String COLUMN_LONGITUDE = "Longitude";
        public static final String COLUMN_LATITUDE = "Latitude";
        public static final String COLUMN_ALTITUDE = "Altitude";
        public static final String COLUMN_HEADING = "Heading";
        public static final String COLUMN_TILT = "Tilt";
        public static final String COLUMN_RANGE = "Range";
        public static final String COLUMN_ALTITUDE_MODE = "Altitude_Mode";
        public static final String COLUMN_HIDE = "Hide";
        public static final String COLUMN_CATEGORY_ID = "Category";

        public static Uri buildPOIUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static String getCompleteNameFromUri(Uri uri) {
            String name = uri.getQueryParameter(COLUMN_COMPLETE_NAME);
            if (null != name && name.length() > 0)
                return name;
            else
                return "Not Found.";
        }
        public static Cursor getAllPOIs(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI,null, null, null, null);
        }
        public static Cursor getPOIsByCategory(FragmentActivity fragmentActivity, String categoryID) {
            return fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = ?",
                    new String[]{categoryID},
                    COLUMN_COMPLETE_NAME);
        }

        public static int countPOIsByCategory(FragmentActivity fragmentActivity, String categoryID) {
            try (Cursor cursor = fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = ?",
                    new String[]{categoryID},
                    COLUMN_COMPLETE_NAME)) {
                return cursor.getCount();
            }
        }

        public static Cursor getPOIsIdByCategory(Context context, String categoryID) {
            return context.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = ?",
                    new String[]{categoryID},
                    COLUMN_ID);
        }

        public static Cursor getNotHiddenPOIsByCategory(FragmentActivity fragmentActivity, String categoryID) {
            return fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = ? AND " + COLUMN_HIDE + " = 0",
                    new String[]{categoryID},
                    COLUMN_COMPLETE_NAME);
        }
        public static int updateCategoryIDByCategoryID(FragmentActivity fragmentActivity, String oldID, String newID) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_CATEGORY_ID, Integer.parseInt(newID));

            return fragmentActivity.getContentResolver().update(CONTENT_URI, contentValues, COLUMN_CATEGORY_ID + " =?", new String[]{oldID});
        }
        public static Uri createNewPOI(FragmentActivity activity, ContentValues contentValues) {
            return activity.getContentResolver().insert(CONTENT_URI, contentValues);
        }
        public static String getCompleteNameByID(FragmentActivity fragmentActivity, String id) {
            try (Cursor cursor = fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    new String[]{COLUMN_COMPLETE_NAME},
                    _ID + " = ?",
                    new String[]{id},
                    null)) {
                if (cursor.moveToNext()) {
                    return cursor.getString(0);
                } else {
                    return "POI not found";
                }
            }
        }

        public static int updateByID(FragmentActivity activity, ContentValues contentValues, String itemSelectedID) {
            String POI_IDselection = _ID + " = ?";
            return activity.getContentResolver().update(CONTENT_URI, contentValues, POI_IDselection, new String[]{itemSelectedID});
        }

        public static Cursor getAllNotHiddenPOIs(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI,null, COLUMN_HIDE + " = 0", null, null);
        }

        public static Cursor getPOIByID(FragmentActivity activity, String id) {
            return activity.getContentResolver().query(CONTENT_URI ,null, _ID + " = ?", new String[]{id}, null);
        }

        public static Cursor getPoiByID(int poiId) {
            return POIsProvider.queryPOIById(poiId);
        }

        public static int deletePOIById(Context context, String poiId) {
            String whereClause = COLUMN_ID + " = ?";
            return context.getContentResolver().delete(CONTENT_URI, whereClause, new String[]{poiId});
        }

        public static int getIdByUri(Uri insertedUri) {
            String uri = insertedUri.toString();
            String[] uriSplit = uri.split("/");
            return Integer.parseInt(uriSplit[uriSplit.length - 1]);
        }
    }

    public static final class CategoryEntry implements BaseColumns {

        //CONTENT_URI = content://com.gsoc.ijosa.liquidgalaxycontroller/category/
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        // Table name
        public static final String TABLE_NAME = "category";

        //Table columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_FATHER_ID = "Father_ID";
        public static final String COLUMN_SHOWN_NAME = "Shown_Name";
        public static final String COLUMN_HIDE = "Hide";

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getIdByUri(Uri insertedUri) {
            String uri = insertedUri.toString();
            String[] uriSplit = uri.split("/");
            return Integer.parseInt(uriSplit[uriSplit.length - 1]);
        }

        public static Cursor getAllCategories(FragmentActivity fragmentActivity){
            return fragmentActivity.getContentResolver().query(CONTENT_URI, null, null, null, null);
        }
        public static Cursor getCategoriesByFatherID(FragmentActivity fragmentActivity, String fatherID) {
            return fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_FATHER_ID + " = ?",
                    new String[]{fatherID},
                    null);
        }

        public static Cursor getCategoriesByName(FragmentActivity fragmentActivity, String categoryName) {
            return fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_NAME + " LIKE ?",
                    new String[]{categoryName},
                    null);
        }

        public static Cursor getNotHiddenCategoriesByFatherID(FragmentActivity fragmentActivity, String fatherID) {
            return fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_FATHER_ID + " = ? AND " + COLUMN_HIDE + " = 0",
                    new String[]{fatherID},
                    null);
        }

        public static Cursor getCategoriesForRefreshing(FragmentActivity fragmentActivity, String whereClause) {
            return fragmentActivity.getContentResolver().query(CONTENT_URI, null, whereClause, null, null);
        }
        public static String getFatherIdByID(FragmentActivity fragmentActivity, String itemSelectedID) {
            try (Cursor queryCursor = fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    new String[]{COLUMN_FATHER_ID},
                    _ID + " = ?",
                    new String[]{itemSelectedID},
                    null)) {
                if (queryCursor.getCount() > 0) {
                    queryCursor.moveToNext();
                    return String.valueOf(queryCursor.getInt(0));
                } else {
                    return "0";
                }
            }
        }

        public static Cursor getIDAndNameByFatherID(FragmentActivity fragmentActivity, String fatherID) {
            return fragmentActivity.getContentResolver().query(
                    CONTENT_URI,
                    new String[]{POIsContract.CategoryEntry._ID, POIsContract.CategoryEntry.COLUMN_NAME},
                    COLUMN_FATHER_ID + " = ?",
                    new String[]{fatherID},
                    null);
        }
        public static int updateFatherIdAndShownNameByID(FragmentActivity fragmentActivity, String id, String fatherID, String shownName) {

            ContentValues updatedValues = new ContentValues();
            updatedValues.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, Integer.parseInt(fatherID));
            updatedValues.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, shownName);

            return fragmentActivity.getContentResolver().update(
                    CONTENT_URI,
                    updatedValues,
                    _ID + " = ?",
                    new String[]{id});
        }
        public static int updateShownNameByID(FragmentActivity fragmentActivity, int id, String shownName) {

            ContentValues updatedValues = new ContentValues();
            updatedValues.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, shownName);

            return fragmentActivity.getContentResolver().update(
                    CONTENT_URI,
                    updatedValues,
                    _ID + " = ?",
                    new String[]{String.valueOf(id)});
        }
        public static Uri createNewCategory(FragmentActivity activity, ContentValues contentValues) throws android.database.SQLException{
            return activity.getContentResolver().insert(CONTENT_URI, contentValues);
        }
        public static Cursor getIDsAndShownNamesOfAllCategories(FragmentActivity fragmentActivity) {
            return fragmentActivity.getContentResolver().query(CONTENT_URI,
                    new String[]{_ID, COLUMN_SHOWN_NAME}, null, null, COLUMN_SHOWN_NAME + " ASC");
        }

        public static Cursor getShownNamesOfAllCategories(FragmentActivity fragmentActivity) {
            return fragmentActivity.getContentResolver().query(CONTENT_URI,
                    new String[]{_ID, COLUMN_SHOWN_NAME}, null, null, COLUMN_SHOWN_NAME + " ASC");
        }

        public static int updateByID(FragmentActivity activity, ContentValues contentValues, String itemSelectedID) {
            String Category_IDselection = _ID + " = ?";
            return activity.getContentResolver().update(CONTENT_URI, contentValues, Category_IDselection, new String[]{itemSelectedID});
        }

        public static String getShownNameByID(FragmentActivity fragmentActivity, int categoryID) {
            try (Cursor c = fragmentActivity.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_SHOWN_NAME},
                    _ID + " = ?", new String[]{String.valueOf(categoryID)}, null)) {

                if (c.getCount() > 0) {
                    c.moveToNext();
                    return c.getString(0);
                } else {
                    return "NO ROUTE";
                }
            }
        }

        public static Cursor getAllNotHiddenCategories(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI,null, COLUMN_HIDE + " = 0", null, null);
        }

        public static int getIdByShownName(FragmentActivity activity, String shownName) {

            try (Cursor c = activity.getContentResolver().query(CONTENT_URI, new String[]{_ID},
                    COLUMN_SHOWN_NAME + " LIKE ?", new String[]{shownName}, null)) {

                if (c != null && c.getCount() == 1) {
                    c.moveToNext();
                    return c.getInt(0);
                } else {
                    return 0;
                }
            }
        }

        public static int getIdByName(FragmentActivity activity, String shownName) {
            try (Cursor c = activity.getContentResolver().query(CONTENT_URI, new String[]{_ID},
                    COLUMN_NAME + " = ?", new String[]{shownName}, null)) {

                if (c != null && c.getCount() == 1) {
                    c.moveToNext();
                    return c.getInt(0);
                } else {
                    return 0;
                }
            }
        }

        public static String getNameById(FragmentActivity activity, int categoryId) {
            try (Cursor c = activity.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_NAME}, COLUMN_ID + " = ?", new String[]{String.valueOf(categoryId)}, null)) {

                if (c != null && c.getCount() == 1) {
                    c.moveToNext();
                    return c.getString(0);
                } else {
                    return "";
                }
            }
        }

        public static String getNameById(Context context, int categoryId) {
            try (Cursor c = context.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_NAME}, COLUMN_ID + " = ?", new String[]{String.valueOf(categoryId)}, null)) {
                if (c != null && c.getCount() == 1) {
                    c.moveToNext();
                    return c.getString(0);
                } else {
                    return "";
                }
            }
        }

        public static Cursor getCategoriesByShownName(FragmentActivity activity, String categoryShownName) {
            return activity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_SHOWN_NAME + " LIKE ?",
                    new String[]{categoryShownName},
                    null);
        }

        public static Cursor getRootCategories(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI, null, COLUMN_FATHER_ID + " = 0", null, COLUMN_ID);
        }

        public static int deleteCategoryById(Context context, String categoryId) {
            String whereClause = COLUMN_ID + " = ?";
            return context.getContentResolver().delete(CONTENT_URI, whereClause, new String[]{categoryId});
        }
    }

    //TAULA TOURS
    public static final class TourEntry implements BaseColumns {

        //CONTENT_URI = content://com.gsoc.ijosa.liquidgalaxycontroller/tour/
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOUR).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOUR;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOUR;

        // Table name
        public static final String TABLE_NAME = "tour";

        //Table columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_CATEGORY_ID = "Category";
        public static final String COLUMN_HIDE = "Hide";
        public static final String COLUMN_INTERVAL = "Interval_of_time";

        public static Uri buildTourUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri createNewTOUR(FragmentActivity activity, ContentValues contentValues) {
            return activity.getContentResolver().insert(CONTENT_URI, contentValues);
        }

        public static Cursor getAllTours(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI, null, null, null, null);
        }

        public static int getIdByUri(Uri insertedUri) {
            String uri = insertedUri.toString();
            String[] uriSplit = uri.split("/");
            return Integer.parseInt(uriSplit[uriSplit.length - 1]);
        }

        public static int updateByID(FragmentActivity activity, ContentValues contentValues, String itemSelectedID) {
            String Tour_IDselection = _ID + " = ?";
            return activity.getContentResolver().update(CONTENT_URI, contentValues, Tour_IDselection, new String[]{itemSelectedID});
        }

        public static Cursor getToursByCategory(FragmentActivity activity, String categoryID) {
            return activity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = ?",
                    new String[]{categoryID},
                    null);
        }

        public static Cursor getNotHiddenToursByCategory(FragmentActivity activity, String category) {
            return activity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = ? AND " + COLUMN_HIDE + " = 0",
                    new String[]{category},
                    null);
        }

        public static Cursor getAllNotHiddenTours(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI, null, COLUMN_HIDE + " = 0", null, null);
        }

        public static Cursor getTourById(FragmentActivity activity, int tourId) {
            return activity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(tourId)},
                    null);
        }

        public static Cursor getNotCategorizedTours(FragmentActivity activity) {
            return activity.getContentResolver().query(
                    CONTENT_URI,
                    null,
                    COLUMN_CATEGORY_ID + " = 0 AND " + COLUMN_HIDE + " = 0",
                    null,
                    null);
        }
    }

    //TAULA TOURPOI
    public static final class TourPOIsEntry implements BaseColumns{
        //CONTENT_URI = content://com.gsoc.ijosa.liquidgalaxycontroller/tourPois/
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOUR_POIS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOUR_POIS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOUR_POIS;

        // Table name
        public static final String TABLE_NAME = "Tour_POIs";

        //Table columns
        public static final String COLUMN_TOUR_ID = "Tour";
        public static final String COLUMN_POI_ID = "POI";
        public static final String COLUMN_POI_ORDER = "POI_Order";
        public static final String COLUMN_POI_DURATION = "POI_Duration";

        public static Uri buildTourUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri createNewTourPOI(FragmentActivity activity, ContentValues contentValues) {
            return activity.getContentResolver().insert(CONTENT_URI, contentValues);
        }

        public static Cursor getPOIsByTourID(String itemSelectedID) {
            return POIsProvider.queryByPoiJOINTourPois(itemSelectedID);
        }
        public static int updateByID(FragmentActivity activity, ContentValues contentValues, String itemSelectedID) {
            String TourPOIs_IDselection = _ID + " = ?";
            return activity.getContentResolver().update(CONTENT_URI, contentValues, TourPOIs_IDselection, new String[]{itemSelectedID});
        }
        public static int updateByTourIdAndPoiID(FragmentActivity activity, ContentValues contentValues, String itemSelectedID, String poiID) {
            String TourPOIs_IDselection = COLUMN_TOUR_ID + " = ? AND " + COLUMN_POI_ID + " = ?";
            return activity.getContentResolver().update(CONTENT_URI, contentValues, TourPOIs_IDselection, new String[]{itemSelectedID, poiID});
        }
        public static int deleteByPoiID(FragmentActivity activity, String poiID) {
            String whereClause = COLUMN_POI_ID + " = ?";
            return activity.getContentResolver().delete(CONTENT_URI, whereClause, new String[]{poiID});
        }

        public static int deleteByTourIdAndPoiID(FragmentActivity activity, String tourID, String poiID) {
            String whereClause = COLUMN_TOUR_ID + " = ? AND " + COLUMN_POI_ID + " = ?";
            return activity.getContentResolver().delete(CONTENT_URI, whereClause, new String[]{tourID, poiID});
        }
    }

    //TAULA LG_TASKS
    public static final class LGTaskEntry implements BaseColumns{
        //CONTENT_URI = content://com.gsoc.ijosa.liquidgalaxycontroller/lgTask/
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LG_TASK).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LG_TASK;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LG_TASK;

        // Table name
        public static final String TABLE_NAME = "LG_TASK";

        //Table columns
        public static final String COLUMN_LG_TASK_ID = "_id";
        public static final String COLUMN_LG_TASK_DESC = "Description";
        public static final String COLUMN_LG_TASK_TITLE= "Title";
        public static final String COLUMN_LG_TASK_SCRIPT = "Script";
        public static final String COLUMN_LG_TASK_IMAGE = "Image";
        public static final String COLUMN_LG_TASK_SHUTDOWNSCRIPT = "Shutdown_Script";
        public static final String COLUMN_LG_TASK_IP = "IP";
        public static final String COLUMN_LG_TASK_USER = "User";
        public static final String COLUMN_LG_TASK_PASSWORD = "Password";
        public static final String COLUMN_LG_BROWSER_URL = "URL";
        public static final String COLUMN_LG_ISRUNNING = "isRunning";

        public static Uri buildTourUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri createNewTask(FragmentActivity activity, ContentValues contentValues) {
            return activity.getContentResolver().insert(CONTENT_URI, contentValues);
        }

        public static Cursor updateTaskStateById(String itemSelectedID, boolean isRunning) {
            POIsProvider.updateTaskStateByTaskId(itemSelectedID, isRunning);
            return getTaskById(itemSelectedID);
        }

        public static Cursor updateTaskURlById(String itemSelectedID, String taskUrl) {
            POIsProvider.updateTaskUrlById(itemSelectedID, taskUrl);
            return getTaskById(itemSelectedID);
        }

        public static Cursor getTaskById(String itemSelectedID) {
            return POIsProvider.queryByTaskId(itemSelectedID);
        }

        public static int updateByTaskId(FragmentActivity activity, ContentValues contentValues,String taskId) {
            String Task_IDselection = COLUMN_LG_TASK_ID + " = ?";
            return activity.getContentResolver().update(CONTENT_URI, contentValues, Task_IDselection, new String[]{taskId});
        }

        public static int deleteByTaskID(FragmentActivity activity, String taskId) {
            String whereClause = COLUMN_LG_TASK_ID + " = ?";
            return activity.getContentResolver().delete(CONTENT_URI, whereClause, new String[]{taskId});
        }

        public static Cursor getAllLGTasks(FragmentActivity activity) {
            return activity.getContentResolver().query(CONTENT_URI, null,null, null, null);
        }
    }
}