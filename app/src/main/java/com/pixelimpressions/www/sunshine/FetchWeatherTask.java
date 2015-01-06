package com.pixelimpressions.www.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.pixelimpressions.www.sunshine.data.WeatherContract;
import com.pixelimpressions.www.sunshine.data.WeatherContract.LocationEntry;
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by mikie on 1/6/15.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private final Context mContext;
    private ArrayAdapter<String> mForecastAdapter;


    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mContext = context;
        mForecastAdapter = forecastAdapter;

    }

    /* The date/time conversion code is going to be moved outside the Async task later,
    * so for convenience we're breaking it out into its own method now.
    */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // Data is fetched in Celsius by default.
        // If user prefers to see in Fahrenheit, convert the values here.
        // We do this rather than fetching in Fahrenheit so that the user can
        // change this option without us having to re-fetch the data once
        // we start storing the values in a database.

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String unitType =
                sharedPrefs.getString(mContext.getString(R.string.pref_tempunits_key),
                        mContext.getString(R.string.pref_temp_units_metric));

        if (unitType.equals(mContext.getString(R.string.pref_temp_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(mContext.getString(R.string.pref_temp_units_metric))) {
            Log.d(LOG_TAG, "unit type not found " + unitType);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
    private long addLocation(String locationSetting, String cityName, double lat, double lon) {
        Log.v(LOG_TAG, "Inserting " + cityName + ", with coord: " + lat + " " + lon);


        //query to see if its previously entered into db
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI, //URI
                new String[]{LocationEntry._ID},//projection
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?", //selection
                new String[]{locationSetting}, //selectionArgs
                null); //sortOrder

        try {
            if (cursor.moveToNext()) {
                Log.v(LOG_TAG, "Found it in the database!");
                int locationIndex = cursor.getColumnIndex(LocationEntry._ID);
                return cursor.getLong(locationIndex);
            } else {
                Log.v(LOG_TAG, "Didn't find it in the database. inserting now!");

                ContentValues locationValues = new ContentValues();
                locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
                locationValues.put(LocationEntry.COLUMN_COORD_LATITUDE, lat);
                locationValues.put(LocationEntry.COLUMN_COORD_LONGITUDE, lon);

                //perform the insert,returns a uri
                Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);
                return ContentUris.parseId(locationInsertUri);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Opps something went wrong! " + e.getMessage());
            return 0;
        } finally {
            cursor.close();
        }

    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays, String locationSetting)
            throws JSONException {

        //These are the names of the JSON objects that need to be extracted

        //Location information
        final String OWN_CITY = "city";
        final String OWN_CITY_NAME = "name";
        final String OWN_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "long";

        //weather information.Each days forecast info is an element of the "list" array
        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        //All temperatures are children of the temp object
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWN_CITY);
        String cityName = cityJson.getString(OWN_CITY_NAME);
        JSONObject coordJson = cityJson.getJSONObject(OWN_COORD);
        double cityLatitude = coordJson.getDouble(OWM_COORD_LAT);
        double cityLongitude = coordJson.getDouble(OWM_COORD_LONG);

        Log.v(LOG_TAG, cityName + ", with coord: " + cityLatitude + " " + cityLongitude);

        //Insert the location into the database
        long locationID = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        String[] resultStrs = new String[numDays];

        //Get and insert the weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

        for (int i = 0; i < weatherArray.length(); i++) {
            //These are the values that will be collected

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            //Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            //The date/time is returned as a long.We need to convert that
            //into something human readable,since most people won't read "1400356800"
            //as "this saturday"
            dateTime = dayForecast.getLong(OWM_DATETIME);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            //description is in a child array called "weather" that is one element long
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            //Temperatures are in a child object called "temp".Try not to name variables
            //temp when working with temperature.it confuses everybody
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationID);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);

            String highAndLow = formatHighLows(high, low);
            String day = getReadableDateString(dateTime);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;

    }


    @Override
    protected String[] doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        if (params.length == 0)
            //nothing to do
            return null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        //query arguments for the url
        String format = "json";
        String units = "metric";
        int numDays = 14;
        String locationQuery = params[0];

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //Query constants
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAMS = "units";
            final String DAYS_PARAM = "cnt";

            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + postCode + "&mode=json&units=metric&cnt=7");
            //build the url using the Uri class by appending to the base url
            Uri builtUrl = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format).appendQueryParameter(UNITS_PARAMS, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).build();

            //construct the actual url for use
            URL url = new URL(builtUrl.toString());
            //Log.v(LOG_TAG, "Built Url " + builtUrl.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Forecast Data: " + forecastJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        //get the parsed JSON from the array
        try {
            return getWeatherDataFromJson(forecastJsonStr, numDays, locationQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String[] results) {
        super.onPostExecute(results);
        //this clears the list and adds the parsed forecast to the list
        if (results != null) {
            mForecastAdapter.clear();
            for (String mForecastStr : results) {
                mForecastAdapter.add(mForecastStr);
            }
        }
    }
}