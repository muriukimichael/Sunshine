package com.pixelimpressions.www.sunshine;

/**
 * Created by mikie on 1/22/15.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixelimpressions.www.sunshine.data.WeatherContract;

import java.util.Date;

/**
 * A placeholder fragment containing a simple view
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOCATION_KEY = "location";

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    private static final String DATE_KEY = "forecast_date";
    //Loader id
    private static final int DETAIL_LOADER = 0;


    private ShareActionProvider mShareActionProvider;
    private String mLocation;
    private String mForecastStr;

    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private ImageView mIconView;
    private TextView mWeatherDesc;
    private TextView mHumidity;
    private TextView mWindSpeed;
    private TextView mPressure;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    /**
     * Saves the location
     * Called to ask the fragment to save its current dynamic state, so it can later be
     * reconstructed in a new instance of its process is restarted. If a new instance of the
     * fragment later needs to be created, the data you place in the Bundle here will be
     * available in the Bundle given to onCreate(Bundle),
     * onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     * <p/>
     * Note:this method may be called at any time before onDestroy().
     * There are many situations where a fragment may be mostly torn down
     * (such as when placed on the back stack with no UI showing),
     * but its state will not be saved until its owning activity actually needs to save its state.
     *
     * @param outState-this is the bundle to save the state into
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCATION_KEY, mLocation);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflate menu to make it visible to the user
        inflater.inflate(R.menu.detailfragment, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.details_tv_date);
        mDateView = (TextView) rootView.findViewById(R.id.details_tv_date_month_day);
        mHighTempView = (TextView) rootView.findViewById(R.id.details_tv_high);
        mLowTempView = (TextView) rootView.findViewById(R.id.details_tv_low);
        mIconView = (ImageView) rootView.findViewById(R.id.details_icon);
        mWeatherDesc = (TextView) rootView.findViewById(R.id.details_tv_description);
        mHumidity = (TextView) rootView.findViewById(R.id.details_tv_humidity);
        mWindSpeed = (TextView) rootView.findViewById(R.id.detais_tv_windspeed);
        mPressure = (TextView) rootView.findViewById(R.id.details_tv_pressure);

        return rootView;
    }

        /*
        * create the intent for use with the share action provider
        * */

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        //ensures that back navigation does not leave app by clearing this task
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return shareIntent;
    }

    /**
     * we initialize the loader here as it is bound to the
     * activity lifecycle.we then update mLocation if
     * one had been saved
     *
     * @param savedInstanceState -bundle containing the saved states
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
    }


    //loader callback methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null || !intent.hasExtra(DATE_KEY)) {
            return null;  //quit at this point if we dint receive the date
        }

        String forecastDate = intent.getStringExtra(DATE_KEY);

        //Sort order: Ascending,by Date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherForLocationWithDate = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, forecastDate);
        Log.v(LOG_TAG, weatherForLocationWithDate.toString());


        //the columns we need
        String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
        };

        //Now create and return a CursorLoader that will take care
        //of creating a Cursor for the data being displayed
        return new CursorLoader(getActivity(), //context
                weatherForLocationWithDate, //Uri
                FORECAST_COLUMNS,//projection
                null, //selection
                null, //selectionArgs
                sortOrder //sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        //we'll use this to get the icon
        int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));

        String dayString = Utility.getDayName(getActivity(),
                data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT)));
        mFriendlyDateView.setText(dayString);

        String monthDayString = Utility.getFormattedMonthDay(getActivity(),
                data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT)));
        mDateView.setText(monthDayString);

        //Check which resource to load based on the date
        Date today = new Date();
        if (data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))
                .equals(WeatherContract.getDbDateString(today))) {
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        } else {
            mIconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }

        String weatherDescription = data.getString(
                data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
        mWeatherDesc.setText(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
        mHighTempView.setText(high);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
        mLowTempView.setText(low);

        float humidity = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
        mHumidity.setText(String.format(getActivity().getString(R.string.format_humidity), humidity));

        float degrees = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
        float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
        mWindSpeed.setText(Utility.getFormattedWind(getActivity(), windSpeed, degrees));

        float pressure = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
        mPressure.setText(String.format(getActivity().getString(R.string.format_pressure), pressure));

        //we need this for the shareIntent
        mForecastStr = String.format(
                "%s - %s -%s/%s", dayString + " " + monthDayString, weatherDescription, high, low);

        Log.v(LOG_TAG, "Forecast String: " + mForecastStr);

        //if onCreateOptionsMenu has already happened,we need to update the share intent noe.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}