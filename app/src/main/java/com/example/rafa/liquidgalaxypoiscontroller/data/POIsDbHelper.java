package com.example.rafa.liquidgalaxypoiscontroller.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class POIsDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "poi_controller.db";
    private static final int DATABASE_VERSION = 33;
    Context context;

    public POIsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createCategoryEntryTable());
        db.execSQL(createPOInEntryTable());
        db.execSQL(createTourEntryTable());
        db.execSQL(createTourPOIsEntryTable());
        db.execSQL(createTasksEntryTable());
        createBaseCategories(db);
        createDefaultLgTasks(db);
        insertDefaultData(db);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        try {
            InputStream inputStream = context.getAssets().open("Inserts.sql");
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.equals("\n") && !line.equals("") && !line.contains("/*")) {
                    db.execSQL(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultLgTasks(SQLiteDatabase db) {
        String sqlLG = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,isRunning) VALUES ('Liquid Galaxy','Launch Liquid Galaxy Task','/home/lg/bin/startup-script.sh','',0)";
        db.execSQL(sqlLG);

        String sqlPotree = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('PCVT','Point Cloud Visualization Tool','/home/lg/asherat666-peruse-a-rue/scripts/lg-potree $lgIp $serverIp 8086 lg','/home/lg/asherat666-peruse-a-rue/scripts/lg-potree-stop $lgIp lg','$serverIp','lg','lq','$serverIp:8086/lg-potree/library',0)";
        db.execSQL(sqlPotree);

        String sqlDLP = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('DLP','Drone Logistics Platform','export DISPLAY=:0 && bash /home/lg/Desktop/DLP/start-dlp $lgIp $serverIp:$serverPort','bash /home/lg/Desktop/DLP/exitdlp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlDLP);

        String sqlPILT = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('PILT','Panoramic Interactive Live Tracker','/home/lg/Desktop/lglab/gsoc16/PILT/pilt-start $lgIp','/home/lg/Desktop/lglab/python-end','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlPILT);

        String sqlFAED = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('FAED','Flying Automated External Defibrilator','/home/lg/Desktop/lglab/gsoc15/FAED/faed-start $lgIp','/home/lg/Desktop/lglab/python-end','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlFAED);

        String sqlVYD = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('VYD','View Your Data','/home/lg/Desktop/lglab/gsoc15/VYD/vyd-start $lgIp','/home/lg/Desktop/lglab/python-end','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlVYD);

    }

    private void createBaseCategories(SQLiteDatabase db) {
        db.execSQL(Earth());
        db.execSQL(Moon());
        db.execSQL(Mars());
        db.execSQL(ImportedFolder());
    }

    private String ImportedFolder() {
        String SQL_CREATE_IMPORTED_FOLDER = "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('PW Beacon Imported',(SELECT _ID FROM CATEGORY WHERE NAME LIKE 'EARTH'), 'PW IMPORTED/', 0);";
        return SQL_CREATE_IMPORTED_FOLDER;
    }

    private String Earth() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('EARTH', 0, 'EARTH/', 0);";
    }

    private String Moon() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MOON', 0, 'MOON/', 0);";
    }

    private String Mars() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MARS', 0, 'MARS/', 0);";
    }

    private String createPOInEntryTable() {
        return "CREATE TABLE poi (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Visited_Place TEXT NOT NULL, Longitude REAL NOT NULL, Latitude REAL NOT NULL, Altitude REAL NOT NULL, Heading REAL NOT NULL, Tilt REAL NOT NULL, Range REAL NOT NULL, Altitude_Mode TEXT NOT NULL, Hide INTEGER NOT NULL, Category INTEGER DEFAULT 0, FOREIGN KEY (Category) REFERENCES category (_id),UNIQUE(Name, Category) ON CONFLICT FAIL  );";
    }

    private String createCategoryEntryTable() {
        return "CREATE TABLE category (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Father_ID INTEGER NOT NULL, Shown_Name TEXT UNIQUE NOT NULL, Hide INTEGER NOT NULL  );";
    }

    private String createTourEntryTable() {
        return "CREATE TABLE tour (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Category INTEGER NOT NULL, Hide INTEGER NOT NULL, Interval_of_time INTEGER NOT NULL, FOREIGN KEY (Category) REFERENCES category (_id)  );";
    }

    private String createTourPOIsEntryTable() {
        return "CREATE TABLE Tour_POIs (_id INTEGER PRIMARY KEY AUTOINCREMENT,Tour INTEGER NOT NULL, POI INTEGER NOT NULL, POI_Order INTEGER NOT NULL, POI_Duration INTEGER DEFAULT 0,  FOREIGN KEY (Tour) REFERENCES tour (_id)  FOREIGN KEY (POI) REFERENCES poi (_id)  );";
    }

    private String createTasksEntryTable() {
        return "CREATE TABLE LG_TASK (_id INTEGER PRIMARY KEY AUTOINCREMENT,Title TEXT NOT NULL, Description TEXT, Script TEXT NOT NULL, Shutdown_Script TEXT NOT NULL, Image BLOB, IP TEXT, User TEXT, Password TEXT,URL TEXT, isRunning INTEGER);";
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS poi");
        db.execSQL("DROP TABLE IF EXISTS tour");
        db.execSQL("DROP TABLE IF EXISTS Tour_POIs");
        db.execSQL("DROP TABLE IF EXISTS LG_TASK");
        onCreate(db);
    }


    public void resetDatabase(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS poi");
        db.execSQL("DROP TABLE IF EXISTS tour");
        db.execSQL("DROP TABLE IF EXISTS Tour_POIs");
        db.execSQL("DROP TABLE IF EXISTS LG_TASK");
        onCreate(db);
    }
}
