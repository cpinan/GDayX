package com.gday.maps;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowsAdapter implements InfoWindowAdapter {

	private View view;

	public CustomWindowsAdapter(Context context) {
		view = ((Activity) context).getLayoutInflater().inflate(
				R.layout.custom_info_layout, null);
	}

	@Override
	public View getInfoContents(final Marker marker) {

		TextView txtPlace = (TextView) view.findViewById(R.id.txtPlace);
		txtPlace.setText(marker.getTitle());

		ImageView imgData = (ImageView) view.findViewById(R.id.imgData);
		imgData.setImageResource(R.drawable.ic_launcher);

		if (GDayMaps.hashBitmaps.get(marker) != null) {
			imgData.setImageBitmap(GDayMaps.hashBitmaps.get(marker));
		}

		return view;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

}
