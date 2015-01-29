package com.pixelimpressions.www.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class DetailsActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        //the detail fragment expects a bundle containing a key date.
        //as we replace it we must therefore ensure that the date String is sent
        //inside a bundle
        if (savedInstanceState == null) {
            //Create the detail fragment and add it to the activity
            //using a fragment transaction
            //get date from intent
            String date = getIntent().getStringExtra(DATE_KEY);

            //save it into bundle for transport
            Bundle arguments = new Bundle();
            arguments.putString(DATE_KEY, date);

            //send it to the detailfragment
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);

            //replace the fragment
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, detailFragment)
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

}
