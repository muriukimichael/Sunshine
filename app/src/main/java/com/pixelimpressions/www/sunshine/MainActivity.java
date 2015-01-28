package com.pixelimpressions.www.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.CallBack {

    private final String LOG_TAG = "MainActivity";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            //The default container view will be present only in the large screen layouts
            //(res/layout-sw600dp).if this view is present,then the activity should be in two pane
            //mode
            mTwoPane = true;

            //in two pane mode,show the detail view in this activity by
            //adding or replacing the detail fragment using a fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_map_preview:
                showPrefLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showPrefLocation() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPrefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();

        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLocation);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.d(LOG_TAG, "Could not call " + location + " , no intent resolver present");
        }

    }

    /**
     * Implementing this method allows the ForecastFragment to notify the DetailActivity
     * that the position selected on the list has changed.This is an implementation of
     * Interfragment communication
     *
     * @param date
     */
    @Override
    public void onItemSelected(String date) {
        Log.v("Date in MainActivity: ", date);
        if (mTwoPane) {
            //in two-pane mode,show the detail view in this activity
            //by adding or replacing the detail fragment using a
            //fragment transaction.
            Bundle args = new Bundle();
            args.putString(DetailsActivity.DATE_KEY, date);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment)
                    .commit();
        } else {
            //start a new activty if in single pane mode
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.DATE_KEY, date);
            startActivity(intent);
        }

    }
}
