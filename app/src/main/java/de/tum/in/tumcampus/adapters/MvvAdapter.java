package de.tum.in.tumcampus.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MVVDeparture;
import de.tum.in.tumcampus.models.MVVObject;
import de.tum.in.tumcampus.models.MVVSuggestion;
import de.tum.in.tumcampus.models.MoodleCourseModule;

/**
 * Created by enricogiga on 17/06/2015.
 */
public class MvvAdapter extends BaseAdapter {


    private MVVObject data;
    private AdapterView.OnItemClickListener listener;
    private LayoutInflater inflater;
    private Context currentContext;

    public static class ViewHolder {
        public ImageView icon;
        public TextView number;
        public TextView station;
        public TextView minutes;

    }

    public MvvAdapter(MVVObject data, AdapterView.OnItemClickListener listener, Context context) {
        this.data = data;
        this.listener = listener;
        this.currentContext = context;
        this.inflater = (LayoutInflater) currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (data != null)
            return data.getResultList().size();
        Utils.log("Warning! dataList is empty!");
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (data != null)
            return data.getResultList().get(position);
        Utils.log("Warning! dataList is empty!");
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.mvv_item, null);

                viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.mvv_icon);
                viewHolder.number = (TextView) convertView.findViewById(R.id.line_number);
                viewHolder.station = (TextView) convertView.findViewById(R.id.station);
                viewHolder.minutes = (TextView) convertView.findViewById(R.id.minutes);

                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            changeStyle(viewHolder, position);

            return convertView;
        } catch (NullPointerException e) {
            Utils.log(e);
            Utils.log(String.format("Erro`1`1111111r: Item %s is null", position));
            return convertView;
        }
    }

    private void changeStyle(ViewHolder viewHolder, int position) {
        if (data.isSuggestion()) {
            MVVSuggestion suggestion = (MVVSuggestion) getItem(position);
            String name = suggestion.getName();

            DisplayMetrics metrics = this.currentContext.getResources().getDisplayMetrics();
            viewHolder.icon.setVisibility(View.GONE);
            viewHolder.number.setVisibility(View.GONE);
            viewHolder.minutes.setVisibility(View.GONE);
            viewHolder.station.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            viewHolder.station.setText(name);
            float fpixels = metrics.density * 80f;
            viewHolder.station.setHeight((int) (fpixels + 0.5f));

        } else if (data.isDeparture()) {
            MVVDeparture departure = (MVVDeparture) getItem(position);
            String number = departure.getLine();
            String station = departure.getDirection();
            String minutes = String.valueOf(departure.getMin() +" min");

            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageDrawable(getImageResource(departure));
            viewHolder.number.setText(number);
            viewHolder.minutes.setText(minutes);
            viewHolder.station.setText(station);

        }
    }

    /**
     * gets the drawable for related departure in mvv
     * because of efficiency used this method, retrieving resources by string name
     * is not efficient.
     *
     * @param departure MVVDeparture object
     * @return Drawable, the icon related to this departure
     */
    public Drawable getImageResource(MVVDeparture departure) {
        MVVDeparture.TransportationType type = departure.getTransportationType();
        Utils.log("type of transportation is " + type);
        switch (type) {
            case UBAHN:
                return currentContext.getResources().getDrawable(R.drawable.mvv_ubahn);
            case SBAHN:
                return currentContext.getResources().getDrawable(R.drawable.mvv_sbahn);
            case BUS_TRAM:
                return currentContext.getResources().getDrawable(R.drawable.mvv_bustram);
        }
        return null;
    }
}
