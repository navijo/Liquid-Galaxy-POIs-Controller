package com.example.rafa.liquidgalaxypoiscontroller.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by RAFA on 19/05/2015.
 */
public class POIsProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static POIsDbHelper mOpenHelper;

    static final int ALL_POIS = 100;
    static final int SINGLE_POI = 101;
    static final int ALL_TOURS = 200;
    static final int SINGLE_TOUR = 201;
    static final int ALL_CATEGORIES = 300;
    static final int SINGLE_CATEGORY = 301;
    static final int ALL_TOUR_POIS = 400;
    static final int SINGLE_TOUR_POIS = 401;

    private static final String POI_IDselection = POIsContract.POIEntry.TABLE_NAME + "." + POIsContract.POIEntry._ID + " = ?";
    private static final String Category_IDselection = POIsContract.CategoryEntry.TABLE_NAME + "." + POIsContract.CategoryEntry._ID + " = ?";
    private static final String Tour_IDselection = POIsContract.TourEntry.TABLE_NAME + "." + POIsContract.TourEntry._ID + " = ?";
    private static final String TourPOIs_IDselection = POIsContract.TourPOIsEntry.TABLE_NAME + "." + POIsContract.TourPOIsEntry._ID + " = ?";

    static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = POIsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, POIsContract.PATH_POI, ALL_POIS);
        matcher.addURI(authority, POIsContract.PATH_POI + "/#", SINGLE_POI);
        matcher.addURI(authority, POIsContract.PATH_CATEGORY, ALL_CATEGORIES);
        matcher.addURI(authority, POIsContract.PATH_CATEGORY + "/#", SINGLE_CATEGORY);
        matcher.addURI(authority, POIsContract.PATH_TOUR, ALL_TOURS);
        matcher.addURI(authority, POIsContract.PATH_TOUR + "/#", SINGLE_TOUR);
        matcher.addURI(authority, POIsContract.PATH_TOUR_POIS, ALL_TOUR_POIS);
        matcher.addURI(authority, POIsContract.PATH_TOUR_POIS + "/#", SINGLE_TOUR_POIS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new POIsDbHelper(getContext());
        return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {

            // "pois/#"
            case SINGLE_POI: {
                //cursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.POIEntry.TABLE_NAME,
                        projection,
                        POI_IDselection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "pois"
            case ALL_POIS: {
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.POIEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // category/#
            case SINGLE_CATEGORY:{
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.CategoryEntry.TABLE_NAME,
                        projection,
                        Category_IDselection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            //categories
            case ALL_CATEGORIES: {
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // tour/#
            case SINGLE_TOUR:{
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.TourEntry.TABLE_NAME,
                        projection,
                        Tour_IDselection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            //tours
            case ALL_TOURS: {
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.TourEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // tourPois/#
            case SINGLE_TOUR_POIS:{
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.TourPOIsEntry.TABLE_NAME,
                        projection,
                        TourPOIs_IDselection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            //tourPois
            case ALL_TOUR_POIS: {
                cursor = mOpenHelper.getReadableDatabase().query(POIsContract.TourPOIsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SINGLE_POI:
                return POIsContract.POIEntry.CONTENT_ITEM_TYPE;
            case ALL_POIS:
                return POIsContract.POIEntry.CONTENT_TYPE;
            case SINGLE_CATEGORY:
                return POIsContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case ALL_CATEGORIES:
                return POIsContract.CategoryEntry.CONTENT_TYPE;
            case SINGLE_TOUR:
                return POIsContract.TourEntry.CONTENT_ITEM_TYPE;
            case ALL_TOURS:
                return POIsContract.TourEntry.CONTENT_TYPE;
            case SINGLE_TOUR_POIS:
                return POIsContract.TourPOIsEntry.CONTENT_ITEM_TYPE;
            case ALL_TOUR_POIS:
                return POIsContract.TourPOIsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case ALL_POIS: {

                long _id = db.insert(POIsContract.POIEntry.TABLE_NAME, null, values);

                if ( _id > 0 )
                    returnUri = POIsContract.POIEntry.buildPOIUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ALL_CATEGORIES: {

                long _id = db.insert(POIsContract.CategoryEntry.TABLE_NAME, null, values);

                if (_id > 0)
                        returnUri = POIsContract.CategoryEntry.buildCategoryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            case ALL_TOURS: {

                long _id = db.insert(POIsContract.TourEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = POIsContract.TourEntry.buildTourUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ALL_TOUR_POIS: {

                long _id = db.insert(POIsContract.TourPOIsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = POIsContract.TourPOIsEntry.buildTourUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        if(selection == null) selection = "1";

        switch (match) {
            case ALL_POIS: {
                rowsDeleted = db.delete(POIsContract.POIEntry.TABLE_NAME, selection, selectionArgs);
                if ( rowsDeleted <= 0 )
                    throw new android.database.SQLException("Failed to delete rows from " + uri);
                break;
            }
            case ALL_CATEGORIES: {
                rowsDeleted = db.delete(POIsContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                if ( rowsDeleted <= 0 )
                    throw new android.database.SQLException("Failed to delete rows from " + uri);
                break;
            }
            case ALL_TOURS:{
                rowsDeleted = db.delete(POIsContract.TourEntry.TABLE_NAME, selection, selectionArgs);
                if ( rowsDeleted <= 0 )
                    throw new android.database.SQLException("Failed to delete rows from " + uri);
                break;
            }
            case ALL_TOUR_POIS:{
                rowsDeleted = db.delete(POIsContract.TourPOIsEntry.TABLE_NAME, selection, selectionArgs);
                if ( rowsDeleted < 0 )
                    throw new android.database.SQLException("Failed to delete rows from " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case ALL_POIS: {
                rowsUpdated = db.update(POIsContract.POIEntry.TABLE_NAME, values, selection, selectionArgs);
                if ( rowsUpdated < 0 )
                    throw new android.database.SQLException("Failed to update rows from " + uri);
                break;
            }
            case ALL_TOURS: {
                rowsUpdated = db.update(POIsContract.TourEntry.TABLE_NAME, values, selection, selectionArgs);
                if ( rowsUpdated < 0 )
                    throw new android.database.SQLException("Failed to update rows from " + uri);
                break;
            }
            case ALL_CATEGORIES: {
                rowsUpdated = db.update(POIsContract.CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
                if ( rowsUpdated < 0 )
                    throw new android.database.SQLException("Failed to update rows from " + uri);
                break;
            }
            case ALL_TOUR_POIS: {
                rowsUpdated = db.update(POIsContract.TourPOIsEntry.TABLE_NAME, values, selection, selectionArgs);
                if ( rowsUpdated < 0 )
                    throw new android.database.SQLException("Failed to update rows from " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    public static Cursor queryByPoiJOINTourPois(String itemSelectedID) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final String sql = "SELECT t.POI, p.Name, t.POI_Duration FROM poi p INNER JOIN Tour_POIs t ON p._id = t.POI WHERE t.Tour = ? ORDER BY t.POI_Order ASC";

        return db.rawQuery(sql, new String[]{itemSelectedID});
    }

    public static void deleteAllDataBase(){
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + POIsContract.CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + POIsContract.POIEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + POIsContract.TourEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + POIsContract.TourPOIsEntry.TABLE_NAME);
        POIsDbHelper.createTables(db);
        POIsDbHelper.createBaseCategories(db);
    }


}
