package com.gsoc.ijosa.liquidgalaxycontroller.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class POIsDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "poi_controller.db";
    private static final int DATABASE_VERSION = 33;
    private Context context;

    POIsDbHelper(Context context) {
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
        try {
            createLgTasks(db);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private JSONObject read() throws IOException {
        JSONObject tasks = null;
        InputStream is = context.getAssets().open("ext_tasks.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        try {
            tasks = new JSONObject(json);
        } catch (Exception e) {
        }
        return tasks;
    }

    private String ext_tasks(JSONObject obj, String task) {
        String temp = null;
        try {
            temp = obj.getString(task);
        } catch (Exception e) {
        }
        return temp;
    }


    private void createLgTasks (SQLiteDatabase db) throws IOException {

        JSONObject obj = read();

        String sqlLG = ext_tasks(obj, "sqlLG");
        db.execSQL(sqlLG);

        String stopLG = ext_tasks(obj, "stopLG");
        db.execSQL(stopLG);

        String sqlPeruse = ext_tasks(obj, "sqlPeruse");
        db.execSQL(sqlPeruse);

        String sqlPotree = ext_tasks(obj, "sqlPotree");
        db.execSQL(sqlPotree);

        String sqlDLP = ext_tasks(obj, "sqlDLP");
        db.execSQL(sqlDLP);

        String sqlPILT = ext_tasks(obj, "sqlPILT");
        db.execSQL(sqlPILT);

        String sqlFAED = ext_tasks(obj, "sqlFAED");
        db.execSQL(sqlFAED);

        String sqlVYD = ext_tasks(obj, "sqlVYD");
        db.execSQL(sqlVYD);

        String sqlIBRI = ext_tasks(obj, "sqlIBRI");
        db.execSQL(sqlIBRI);

        String sqlFlOYBD = ext_tasks(obj, "sqlFlOYBD");
        db.execSQL(sqlFlOYBD);

        String sqlWikimediaDataProject = ext_tasks(obj, "sqlWikimediaDataProject");
        db.execSQL(sqlWikimediaDataProject);

        String sqlmy_meteorological_station = ext_tasks(obj, "sqlmy_meteorological_station");
        db.execSQL(sqlmy_meteorological_station);

        String sqlmemories = ext_tasks(obj, "sqlmemories");
        db.execSQL(sqlmemories);

        String sqlSmartAgroVisualizationTool = ext_tasks(obj, "sqlSmartAgroVisualizationTool");
        db.execSQL(sqlSmartAgroVisualizationTool);

    }

    private void createBaseCategories(SQLiteDatabase db) {
        db.execSQL(Earth());
        db.execSQL(Moon());
        db.execSQL(Mars());
        db.execSQL(ImportedFolder());
    }

    private String ImportedFolder() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('PW Beacon Imported',(SELECT _ID FROM CATEGORY WHERE NAME LIKE 'EARTH'), 'PW IMPORTED/', 0);";
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


    void resetDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS poi");
        db.execSQL("DROP TABLE IF EXISTS tour");
        db.execSQL("DROP TABLE IF EXISTS Tour_POIs");
        db.execSQL("DROP TABLE IF EXISTS LG_TASK");
        onCreate(db);
    }
}
