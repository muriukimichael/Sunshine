package com.pixelimpressions.www.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mikie on 11/6/14.
 */
public class WeatherContract {
    /*
    Defines the table and contents of the weather table
    * */

    //add the authority
    /*The Content Authority is a name for the entire content provider,similar to the
    * relationship between a domain name and its website.A convenient string to use for the
    * content authority is the package name of the app,which is guaranteed to be unique on the device.
    * */
    public static final String CONTENT_AUTHORITY = "com.pixelimpressions.www.sunshine";

    //use the CONTENT AUTHORITY to create the
    // base of all URI'S which apps will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //possible paths(these are appended to the base content URI for possible URI's)
    //for instance,content://com.pixelimpressions.www.sunshine/weather is a valid path for
    //looking at weather data.content:///com.pixelimpressions.www.sunshine/givemeroot/ will fail
    //as the content provide hasnt been given any information on what to do with "givemeroot".
    //at lease lets hope not.Don't be that dev mike Done be that dev

    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    //Format used for storing date in the databse.also used for converting those strings
    //back into date objects for comparison/processing
    public static final String DATE_FORMAT = "yyyyMMdd";


    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup.
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    public static String getDbDateString(Date date) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * Converts Date class to a String representation,used for easy comparison and database lookup.
     *
     * @param dateText-The input date
     * @return the Date Object
     */
    public static Date getDbDateString(String dateText) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            return dbDateFormat.parse(dateText);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static final class LocationEntry implements BaseColumns {
        /*inner class that defines the contents of the location table*/

        //content provider values
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        //database column names
        //table name
        public static final String TABLE_NAME = "location";        //special mime types that tell the content provide whether this
        //the location setting string is what us sent to openweathermap as the location query
        public static final String COLUMN_LOCATION_SETTING = "location_setting";        // is a list(dir) of items or an item
        //name of the city stored as text human readable location string provided by the API
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        //longitude and latitude this is obtained after launching the mapintent
        // so we can store the latitude and longitude as returned by openweathermap
        public static final String COLUMN_COORD_LONGITUDE = "coord_long";
        public static final String COLUMN_COORD_LATITUDE = "coord_lat";

        //a uri call with only an id for querying a single location item
        public static Uri buildLocationUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;


    }


    public static final class WeatherEntry implements BaseColumns {
        /*Inner class that defines the contents of the weather table*/

        //Content provider values
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();
        //Database values
        public static final String TABLE_NAME = "weather";
        //column with the foreign key into the location table
        public static final String COLUMN_LOC_KEY = "location_id";
        //DATE,stored as TEXT with format yyyy-mm-dd
        public static final String COLUMN_DATETEXT = "date";
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        //weather id as returned buy the API,to identify the icon to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";
        //Short description and long description as returned by the weather api
        public static final String COLUMN_SHORT_DESC = "short_desc";
        //min and max temperatures for the day stored as floats
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String COLUMN_MAX_TEMP = "max";
        //humidity is stored as a float representing the percentage
        public static final String COLUMN_HUMIDITY = "humidity";
        //pressure is stored as a float representing the percentage
        public static final String COLUMN_PRESSURE = "pressure";
        //wind speed is stored as a float representing wind speed in mph
        public static final String COLUMN_WIND_SPEED = "wind";
        //Degrees are the meteorological degrees (e.g 0 is north and 180 is south),stored as floats
        public static final String COLUMN_DEGREES = "degrees";

        //a uri call with only an id for querying a single weather item
        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate
                (String locationSetting, String startDate) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATETEXT, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, String date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        //decodes the uri structure
        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }


        /*these are uri builder and decoder functions
         *they reduce the places in code that have the actual uri
         * */

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATETEXT);
        }


    }


}
