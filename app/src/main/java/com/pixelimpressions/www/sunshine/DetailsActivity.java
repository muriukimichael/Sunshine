package com.pixelimpressions.www.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixelimpressions.www.sunshine.data.WeatherContract;
import com.pixelimpressions.www.sunshine.data.WeatherContract.WeatherEntry;


public class DetailsActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";
    private static final String LOCATION_KEY = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

        private static final String DATE_KEY = "forecast_date";
        //Loader id
        private static final int DETAIL_LOADER = 0;
        private static final String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP
        };
        private ShareActionProvider mShareActionProvider;
        private String mLocation;
        private String mForecastStr;


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
            return inflater.inflate(R.layout.fragment_details, container, false);
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
         * we intialize the loader here as it is bound to the
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

            Uri weatherForLocationWithDate = WeatherEntry.buildWeatherLocationWithDate(
                    mLocation, forecastDate);
            Log.v(LOG_TAG, weatherForLocationWithDate.toString());

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

            String dateString = Utility.formatDate(
                    data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
            ((TextView) getView().findViewById(R.id.details_tv_date)).setText(dateString);

            String weatherDescription = data.getString(
                    data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            ((TextView) getView().findViewById(R.id.details_tv_description))
                    .setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(getActivity(),
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.details_tv_high)).setText(high);

            String low = Utility.formatTemperature(getActivity(),
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.details_tv_low)).setText(low);

            //we need this for the shareIntent
            mForecastStr = String.format(
                    "%s - %s -%s/%s", dateString, weatherDescription, high, low);

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
}
