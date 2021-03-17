package com.example.techsolutionproject.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.example.techsolutionproject.R;

public class CustomeInfoWindow implements GoogleMap.InfoWindowAdapter {


    View myView;

    public CustomeInfoWindow(Context context)
    {
        myView = LayoutInflater.from(context)
                .inflate(R.layout.custome_user_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtPickupTitle = ((TextView)myView.findViewById(R.id.txtPickupInfo));
        txtPickupTitle.setText(marker.getTitle());

        TextView txtPickupSnippet= ((TextView)myView.findViewById(R.id.txtPickupSnippet));
        txtPickupSnippet.setText(marker.getSnippet());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}