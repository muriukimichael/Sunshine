package com.pixelimpressions.www.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Weather Fragment
 */
public class ForecastFragment extends Fragment {

    private static final String DEBUG_TAG = "ForecastFragment";
    private ArrayAdapter<String> mForecastAdapter;


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
}