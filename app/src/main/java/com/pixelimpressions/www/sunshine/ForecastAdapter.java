package com.pixelimpressions.www.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */

public class ForecastAdapter extends CursorAdapter {

    //string representations of the view to return
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            //this is for today
            return VIEW_TYPE_TODAY;
        } else {
            //every other layout
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    @Override
    public int getViewTypeCount() {
        //we have two different layouts to populate the list with
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //creates a new view for displaying data from a cursor
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        //inflate the multiple views for today and the rest of the days
        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }
        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //bind an existing view to the data provided by a cursor
        //read weather ID from the cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //use placeholder for now
        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);

        //Read date from the cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        //find the textview and set the formatted date on it
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context, dateString));

        //read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //Find the textview and set weather forecast on it.
        TextView descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        descriptionView.setText(description);

        //Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        //Read high temp from cursor
        Double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        //Find the high TexView and set the temperature on it
        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        highView.setText(Utility.formatTemperature(high, isMetric));

        //Read low temperature from cursor
        Double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //Find the low TextView and set the temperature on it
        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        lowView.setText(Utility.formatTemperature(low, isMetric));

    }
}
