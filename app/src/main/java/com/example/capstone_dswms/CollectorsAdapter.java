package com.example.capstone_dswms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CollectorsAdapter extends ArrayAdapter<CollectorsFragment.Collector> {
    private LayoutInflater inflater;

    public CollectorsAdapter(Context context, List<CollectorsFragment.Collector> collectorsList) {
        super(context, 0, collectorsList);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_collectors, parent, false);
        }

        CollectorsFragment.Collector collector = getItem(position);
        if (collector != null) {
            TextView mobileNumberTextView = convertView.findViewById(R.id.mobileNumberTextView);
            TextView firstNameTextView = convertView.findViewById(R.id.firstNameTextView);
            TextView lastNameTextView = convertView.findViewById(R.id.lastNameTextView);

            mobileNumberTextView.setText(collector.getMobileNumber());
            firstNameTextView.setText(collector.getFirstName());
            lastNameTextView.setText(collector.getLastName());
        }

        return convertView;
    }
}

