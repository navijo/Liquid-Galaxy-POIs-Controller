package com.example.rafa.liquidgalaxypoiscontroller.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.TourEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.CategoryEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.POIEntry;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract.TourPOIsEntry;

/**
 * Created by RAFA on 18/05/2015.
 */
public class POIsDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 18;
    static final String DATABASE_NAME = "poi_controller.db";

    public POIsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createCategoryEntryTable());
        db.execSQL(createPOInEntryTable());
        db.execSQL(createTourEntryTable());
        db.execSQL(createTourPOIsEntryTable());
    }

    private String createPOInEntryTable() {
        // Create a table to hold POIs.
        final String SQL_CREATE_POI_TABLE = "CREATE TABLE " + POIsContract.POIEntry.TABLE_NAME + " (" +
                POIEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                POIEntry.COLUMN_COMPLETE_NAME + " TEXT UNIQUE NOT NULL, " +
                POIEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                POIEntry.COLUMN_VISITED_PLACE_NAME + " TEXT NOT NULL, " +
                POIEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                POIEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                POIEntry.COLUMN_ALTITUDE + " REAL NOT NULL, " +
                POIEntry.COLUMN_HEADING + " REAL NOT NULL, " +
                POIEntry.COLUMN_TILT + " REAL NOT NULL, " +
                POIEntry.COLUMN_RANGE + " REAL NOT NULL, " +
                POIEntry.COLUMN_ALTITUDE_MODE + " TEXT NOT NULL, " +
                POIEntry.COLUMN_HIDE + " INTEGER NOT NULL, " +
                POIEntry.COLUMN_CATEGORY_ID + " INTEGER DEFAULT 0," +
                " FOREIGN KEY (" + POIEntry.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ") " +
                " );";

        return SQL_CREATE_POI_TABLE;
    }
    private String createCategoryEntryTable(){

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + POIsContract.CategoryEntry.TABLE_NAME + " (" +
                CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CategoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                CategoryEntry.COLUMN_FATHER_ID + " INTEGER NOT NULL, " +
                CategoryEntry.COLUMN_SHOWN_NAME + " TEXT UNIQUE NOT NULL, " +
                CategoryEntry.COLUMN_HIDE + " INTEGER NOT NULL " +
                " );";
        return SQL_CREATE_CATEGORY_TABLE;
    }
    private String createTourEntryTable(){

        final String SQL_CREATE_TOUR_TABLE = "CREATE TABLE " + POIsContract.TourEntry.TABLE_NAME + " (" +
                TourEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TourEntry.COLUMN_NAME + " TEXT NOT NULL, " +
//                TourEntry.COLUMN_ITINERARY + " TEXT NOT NULL, " +
                TourEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                TourEntry.COLUMN_HIDE + " INTEGER NOT NULL, " +
                TourEntry.COLUMN_INTERVAL + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + TourEntry.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ") " +
                " );";

        return SQL_CREATE_TOUR_TABLE;
    }

    private String createTourPOIsEntryTable(){

        final String SQL_CREATE_TOUR_TABLE = "CREATE TABLE " + POIsContract.TourPOIsEntry.TABLE_NAME + " (" +
                TourPOIsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TourPOIsEntry.COLUMN_TOUR_ID + " INTEGER NOT NULL, " +
                TourPOIsEntry.COLUMN_POI_ID + " INTEGER NOT NULL, " +
                TourPOIsEntry.COLUMN_POI_ORDER + " INTEGER NOT NULL, " +
                TourPOIsEntry.COLUMN_POI_DURATION + " INTEGER DEFAULT 0, " +
                " FOREIGN KEY (" + TourPOIsEntry.COLUMN_TOUR_ID + ") REFERENCES " +
                TourEntry.TABLE_NAME + " (" + TourEntry._ID + ") " +
                " FOREIGN KEY (" + TourPOIsEntry.COLUMN_POI_ID + ") REFERENCES " +
                POIEntry.TABLE_NAME + " (" + POIEntry._ID + ") " +
                " );";

        return SQL_CREATE_TOUR_TABLE;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + POIEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TourEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TourPOIsEntry.TABLE_NAME);
        onCreate(db);
    }
}
