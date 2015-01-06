package com.pixelimpressions.www.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pixelimpressions.www.sunshine.data.WeatherContract.LocationEntry;
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by mikie on 11/7/14.
 * This class handles the creation and updating
 * of database tables weather and location i
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //create the location table to hold locations.A location consists of the string
        // supplied in the location setting.the city name, and the latitude and longitude

        //location table
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME
                + " (" + LocationEntry._ID + " INTEGER PRIMARY KEY, "
                + LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, "

                + LocationEntry.COLUMN_COORD_LATITUDE + " REAL NOT NULL, "
                + LocationEntry.COLUMN_COORD_LONGITUDE + " REAL NOT NULL, "

                //ensures that an insert with the same location will not succeed therefore not return an id
                + " UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE" + " );";

        //weather table
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME
                + " (" + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                //foreign key from the location table
                + WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, "
                + WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, "
                + WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
                + WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, "

                + WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, "

                + WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
                + WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "

                //Set up the location column as a foreign key to the location table.
                + "FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES "
                + LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "),"

                //To assure the application have just one weather entry per day
                //per location,it's created a UNIQUE constraint with REPLACE strategy
                + " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ","
                + WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        //execute the sql statement
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //the standard is to drop existing tables and recreate them for the upgrade
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
