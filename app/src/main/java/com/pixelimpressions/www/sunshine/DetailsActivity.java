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
     *
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = "#Sunshine";
        private static final int DETAIL_LOADER = 0;
        private String mForecastStr;
        private TextView dateTextView, descriptionTextView, highTextView, lowTextView;


        public DetailFragment() {

            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            dateTextView = (TextView) getActivity().findViewById(R.id.details_tv_date);
            descriptionTextView = (TextView) getActivity().findViewById(R.id.details_tv_description);
            highTextView = (TextView) getActivity().findViewById(R.id.details_tv_high);
            lowTextView = (TextView) getActivity().findViewById(R.id.details_tv_low);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            //inflate menu to make it visible to the user
            inflater.inflate(R.menu.detailfragment, menu);
            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this ShareActionProvider.  You can update this at any time,
            // like when the user selects a new piece of data they might like to share.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_details, container, false);
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
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
            Log.d(LOG_TAG, "Sharing");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            Log.d(LOG_TAG, "Sharing done");
            return shareIntent;

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String locationSetting = Utility.getPreferredLocation(getActivity());
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
            Uri weatherLocationWithDate = WeatherEntry.buildWeatherLocationWithDate(locationSetting, mForecastStr);

            return new CursorLoader(getActivity(),
                    weatherLocationWithDate,
                    new String[]{
                            WeatherEntry.COLUMN_DATETEXT,
                            WeatherEntry.COLUMN_SHORT_DESC,
                            WeatherEntry.COLUMN_MAX_TEMP,
                            WeatherEntry.COLUMN_MIN_TEMP
                    },
                    null,
                    null,
                    sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.moveToFirst()) {
                String dateString = Utility.formatDate(data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
                dateTextView.setText(dateString);

                String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
                descriptionTextView.setText(description);

                boolean isImperial = Utility.isMetric(getActivity());

                String high = Utility.formatTemperature(data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isImperial);
                highTextView.setText(high);

                String low = Utility.formatTemperature(data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isImperial);
                lowTextView.setText(low);

            } else {
                Log.v(LOG_TAG, "No data in cursor :-)");
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            dateTextView.setText("");
            descriptionTextView.setText("");
            highTextView.setText("");
            lowTextView.setText("");
        }
    }
}
