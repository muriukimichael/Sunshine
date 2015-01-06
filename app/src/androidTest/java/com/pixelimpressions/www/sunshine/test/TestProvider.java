package com.pixelimpressions.www.sunshine.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.pixelimpressions.www.sunshine.data.WeatherContract.LocationEntry;
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
                null,
                null);

        mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                null,
                null);

        Cursor cursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        assertEquals(cursor.getCount(), 0);
        cursor.close();
    }


    public void testInsertReadProvider() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.

        ContentValues testValues = TestDb.createNorthPoleLocationValues();

        long locationRowId;
        Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        locationRowId = ContentUris.parseId(locationInsertUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        //test the location
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        //Test if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(LocationEntry.buildLocationUri(locationRowId), //build the location uri  with a location row id
                null, // leaving "columns" null just returns all columns
                null, // cols for where clause
                null, // values for "where" clause
                null); // sort order

        //validate the results from cursor
        TestDb.validateCursor(cursor, testValues);


        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);

        //insert into the content provider
        Uri insertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        long weatherRowId = ContentUris.parseId(insertUri);
        assertTrue(weatherRowId != -1);


        // test the weather insert
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(weatherCursor, weatherValues);


        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(weatherValues, testValues);

        // Get the joined Weather and Location data
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(
                        TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        //get the weather data with a specific date
        weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null,
                null,
                null,
                null);
        TestDb.validateCursor(weatherCursor, weatherValues);


    }


    public void testGetType() {
        //content://com.pixelimpressions.www.sunshine/weather/
        //this is the way we access the Content Provider
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        //vnd.android.cursor.dir/com.pixelimpressions.www.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        //content://com.pixelimpressions.www.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        //vnd.android.cursor.dir/com.pixelimpressions.www.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        //content://com.pixelimpressions.www.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        //vnd.android.cursor.item/com.pixelimpressions.www.sunshine/weather/
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        //content://com.pixelimpressions.www.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        //vnd.android.cursor.dir/com.pixelimpressions.www.sunshine/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        //content://com.pixelimpressions.www.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        //vnd.android.cursor.item/com.pixelimpressions.www.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);

    }


    public void testUpdateLocation() {
        //create a map of values where column names are keys
        ContentValues values = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        //verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI,
                updatedValues,
                LocationEntry._ID + "= ?",
                new String[]{Long.toString(locationRowId)});


        assertEquals(count, 1);
    }


    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }
}