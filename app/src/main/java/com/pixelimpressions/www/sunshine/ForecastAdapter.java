package com.pixelimpressions.www.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixelimpressions.www.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */

public class ForecastAdapter extends CursorAdapter {

    //string representations of the view to return
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    //a public setter method to allow us to set whether to use the today layout or not
    //we want to use the today layout on tablets only
    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    /**
     * This method tells us whether to use the Today list item or just populate the list
     * using the normal list item layout.This depends on whether the the device is a phone or a
     * tablet
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
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
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        //call the view holder
        ViewHolder holder = new ViewHolder(view);
        //save the object view in the layout using the setTag method
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //bind an existing view to the data provided by a cursor

        //Our ViewHolder already has a reference to the views we need so we use it to set the
        //content as opposed to the performance costly findViewById calls
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //read weather ID from the cursor
        int weatherId = cursor.getInt(
                cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        //use placeholder for now

        int viewType = getItemViewType(cursor.getPosition());

        switch (viewType) {

            case VIEW_TYPE_TODAY:
                //get weather icon
                viewHolder.iconView.setImageResource(
                        Utility.getArtResourceForWeatherCondition(weatherId));
                break;

            case VIEW_TYPE_FUTURE_DAY:
                //get weather icon
                viewHolder.iconView.setImageResource(
                        Utility.getIconResourceForWeatherCondition(weatherId));
                break;
        }


        //Read date from the cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        //find the textview and set the formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

        //read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //Find the textview and set weather forecast on it.
        viewHolder.descriptionView.setText(description);

        //Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        //Read high temp from cursor
        Double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        //Find the high TexView and set the temperature on it
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high, isMetric));

        //Read low temperature from cursor
        Double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //Find the low TextView and set the temperature on it
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low, isMetric));
    }


    /**
     * ViewHolder(Cached) of children views for a
     * forecast_list_item
     */
    public static class ViewHolder {

        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {

            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);

        }
    }
}
