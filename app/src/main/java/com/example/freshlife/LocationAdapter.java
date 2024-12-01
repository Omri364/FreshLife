package com.example.freshlife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private Context context;
    private List<String> locations;
    private OnDeleteLocationListener onDeleteLocationListener;

    public LocationAdapter(Context context, List<String> locations, OnDeleteLocationListener listener) {
        this.context = context;
        this.locations = locations;
        this.onDeleteLocationListener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String location = locations.get(position);

        // Set location name
        holder.locationName.setText(location);

        // Set location name's color
        holder.locationName.setTextColor(ContextCompat.getColor(context, R.color.secondaryTextColor));

        // Disable delete button for "All" and "Unsorted"
        if (location.equals("All") || location.equals("Unsorted")) {
            holder.deleteButton.setVisibility(View.GONE); // Hide delete button
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                onDeleteLocationListener.onDeleteClick(position);
            });
        }
    }


    @Override
    public int getItemCount() {
        return locations.size();
    }

    public interface OnDeleteLocationListener {
        void onDeleteClick(int position);
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        ImageButton deleteButton;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
