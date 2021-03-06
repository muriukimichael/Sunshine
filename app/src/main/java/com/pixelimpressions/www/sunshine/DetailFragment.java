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
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;


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
    private String mDateStr;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mDateStr = arguments.getString(DetailsActivity.DATE_KEY);
        }

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

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


    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailsActivity.DATE_KEY)
                && mLocation != null &&
                !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
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

        //if onLoadFinished happens before this,we can go ahead and set the share intent now.
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //ensures that back navigation does not leave app by clearing this task
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
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

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
        //if we have the date key lets restart the loader else fallback to placeholder data
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailsActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }


    //loader callback methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.v(LOG_TAG, "In onCreateLoader");

        //Sort order: Ascending,by Date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherForLocationWithDate = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, mDateStr);
        Log.v(LOG_TAG, weatherForLocationWithDate.toString());


        //the columns we need
        String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_DEGREES,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_WEATHER_ID
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

        if (data != null && data.moveToFirst()) {
            //get weather icon and set it
            int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            //use weather art image
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            //format the dates and present them
            String dayString = Utility.getDayName(getActivity(),
                    data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
            mFriendlyDateView.setText(dayString);

            String monthDayString = Utility.getFormattedMonthDay(getActivity(),
                    data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
            mDateView.setText(monthDayString);


            //the weather description
            String weatherDescription = data.getString(
                    data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            mWeatherDesc.setText(weatherDescription);

            //implements accessibility
            mIconView.setContentDescription(weatherDescription);

            //format temperature values and display
            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(getActivity(),
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            mHighTempView.setText(high);

            String low = Utility.formatTemperature(getActivity(),
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            mLowTempView.setText(low);

            //humidity values
            float humidity = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
            mHumidity.setText(String.format(getActivity().getString(R.string.format_humidity), humidity));

            //wind
            float degrees = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_DEGREES));
            float windSpeed = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
            mWindSpeed.setText(Utility.getFormattedWind(getActivity(), windSpeed, degrees));

            //pressure
            float pressure = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
            mPressure.setText(String.format(getActivity().getString(R.string.format_pressure), pressure));

            //we need this for the shareIntent
            mForecastStr = String.format(
                    "%s - %s -%s/%s", dayString + " " + monthDayString, weatherDescription, high, low);

            Log.v(LOG_TAG, "Forecast String: " + mForecastStr);

            //if onCreateOptionsMenu has already happened,we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}