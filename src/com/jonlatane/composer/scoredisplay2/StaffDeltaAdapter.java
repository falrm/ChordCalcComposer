package com.jonlatane.composer.scoredisplay2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jonlatane.composer.R;
import com.jonlatane.composer.music.Score.Staff.StaffDelta;

public class StaffDeltaAdapter extends ArrayAdapter<StaffDelta> {
    private LayoutInflater mInflater;
    
    StaffDelta[] values;

    public StaffDeltaAdapter(Context context, StaffDelta[] values) {
        super(context, R.layout.staffdeltaview, values);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            // Inflate the view since it does not exist
            convertView = mInflater.inflate(R.layout.staffdeltaview, parent, false);

            // Create and save off the holder in the tag so we get quick access to inner fields
            // This must be done for performance reasons
            holder = new Holder();
            //holder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        // Populate the text
        //holder.textView.setText(getItem(position).);

        // Set the color
        //convertView.setBackgroundColor(getItem(position).getBackgroundColor());

        return convertView;
    }

    /** View holder for the views we need access to */
    private static class Holder {
        public TextView textView;
    }
}
