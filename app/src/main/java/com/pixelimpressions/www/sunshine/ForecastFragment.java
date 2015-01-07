package com.pixelimpressions.www.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pixelimpressions.www.sunshine.data.WeatherContract;
import com.pixelimpressions.www.sunshine.data.WeatherContract.LocationEntry;
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            LocationEntry.COLUMN_LOCATION_SETTING
    };
    private static final int FORECAST_LOADER = 0;
    private ArrayAdapter<String> mForecastAdapter;
    private String mLocation;


    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<String>();

        //create an ArrayAdapter for handling the data and ListView
        mForecastAdapter = new ArrayAdapter<String>(
                //access the parent class context
                getActivity(),
                //the layout for the List Item
                R.layout.list_item_forecast,
                //the List View textview
                R.id.list_item_forecast_textview,
                //the data
                weekForecast);

        ListView fListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        fListView.setAdapter(mForecastAdapter);
        fListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
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
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}