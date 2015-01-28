package com.pixelimpressions.www.sunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pixelimpressions.www.sunshine.data.WeatherContract;
import com.pixelimpressions.www.sunshine.data.WeatherContract.LocationEntry;
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;

import java.util.Date;

/**
 * Weather Fragment
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //The indices are tied to FORECAST_COLUMNS.IF FORECAST_COLUMNS changes,these
    //must change
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    private static final String DEBUG_TAG = "ForecastFragment";

    //for the forecast view we are showing only a small subset of the stored data.
    //Specify the columns we need
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter mForecastAdapter;
    private String mLocation;


    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add this line in order for the fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        //update the loader when the activity resumes
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        //get a reference to the listview,and attach this adapter to it.
        ListView fListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        fListView.setAdapter(mForecastAdapter);
        fListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //get date from cursor,format it and bundle up as we start the DetailsActivity
                Cursor cursor = mForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    //notify the activity the position clicked has changed using the callback interface
                    Log.v("Sent date", cursor.getString(COL_WEATHER_DATE));
                    ((CallBack) getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //initialize the loader as its lifecycle is bound to the activity not the fragment
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //new FetchWeatherTask().execute(String.valueOf(94043));
                //new FetchWeatherTask().execute("Nairobi,Kenya");
                //updated to use the user location setting
                updateWeather();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.execute(location);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        //Sort order:Ascending, by Date
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        //Now create and return a CursorLoader that will take care of
        //creating a cursor from the data being displayed
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    //use the data from the loader
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update the UI.called automatically when a Loader has finished its load
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is reset,we don't have any data so we just pull the cursor to null
        // this removes any reference to data previously held
        mForecastAdapter.swapCursor(null);
    }


    /**
     * Created by mikie on 1/27/15.
     * A callback interface that all activities containing this
     * fragment must implement.This mechanism allows activities to be
     * notified of item selections in order to handle the change
     */
    public interface CallBack {

        /**
         * Callback for when an item has been selected
         */
        public void onItemSelected(String date);

    }

}