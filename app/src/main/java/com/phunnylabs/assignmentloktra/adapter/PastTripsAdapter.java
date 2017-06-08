package com.phunnylabs.assignmentloktra.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.phunnylabs.assignmentloktra.R;
import com.phunnylabs.assignmentloktra.models.Trip;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import io.realm.RealmResults;

/**
 * Created by sachin on 06/06/17.
 */

public class PastTripsAdapter extends BaseAdapter {
    Context mContext;
    RealmResults<Trip> mPastTrips;

    public PastTripsAdapter(Context context, RealmResults<Trip> pastTrips) {
        mContext = context;
        mPastTrips = pastTrips;
    }

    @Override
    public int getCount() {
        return mPastTrips.size();
    }

    @Override
    public Trip getItem(int i) {
        return mPastTrips.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (null == convertView) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            convertView = inflater.inflate(R.layout.past_trip_item, viewGroup, false);
            viewHolder = new ViewHolder();

            viewHolder.startPoint = (TextView) convertView.findViewById(R.id.textViewStartTrip);
            viewHolder.endPoint = (TextView) convertView.findViewById(R.id.textViewEndTrip);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.textViewDistance);
            viewHolder.time = (TextView) convertView.findViewById(R.id.textViewTime);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        viewHolder.startPoint.setText(UtilityClass.getAddress(mContext,startPoint.latitude,startPoint.longitude));
//        viewHolder.endPoint.setText(UtilityClass.getAddress(mContext,endPoint.latitude,endPoint.longitude));

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        String distance = df.format(mPastTrips.get(position).getTripDistance());

        viewHolder.distance.setText(distance + " Mtrs");
        viewHolder.time.setText(getItem(position).getTripTime() + " Secs");

        return convertView;
    }

    private class ViewHolder {
        TextView startPoint, endPoint, distance, time;
    }
}
