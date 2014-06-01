package com.gday.maps;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class SampleActivity extends ActionBarActivity {

	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {

			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			if (map == null)
				return;

			_init();

		}
	}

	private void _init() {

		// map.getUiSettings().setZoomControlsEnabled(false);

		currentLocation();
	}

	// Hello Map
	public void test01() {

		LatLng sydney = new LatLng(-33.867, 151.206);

		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

		map.addMarker(new MarkerOptions().title("Sydney")
				.snippet("The most populous city in Australia.")
				.position(sydney));
	}

	// Map types
	public void test02() {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-18.142,
				178.431), 2));

		// Other supported types include: MAP_TYPE_NORMAL,
		// MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	}

	// Indoor map
	public void test03() {
		// Some buildings have indoor maps. Center the camera over
		// the building, and a floor picker will automatically appear.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.86997,
				151.2089), 18));
	}

	// Custom marker and info windows
	public void test04() {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.889,
				-87.622), 16));

		// You can customize the marker image using images bundled with
		// your app, or dynamically generated bitmaps.
		map.addMarker(new MarkerOptions()
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_launcher))
				.anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
				.position(new LatLng(41.889, -87.622)));
	}

	// Flat marker
	public void test05() {
		LatLng mapCenter = new LatLng(41.889, -87.622);

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 13));

		// Flat markers will rotate when the map is rotated,
		// and change perspective when the map is tilted.
		map.addMarker(new MarkerOptions()
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.direction_arrow))
				.position(mapCenter).flat(true).rotation(245));

		CameraPosition cameraPosition = CameraPosition.builder()
				.target(mapCenter).zoom(13).bearing(90).build();

		// Animate the change in camera view over 2 seconds
		map.animateCamera(
				CameraUpdateFactory.newCameraPosition(cameraPosition), 2000,
				null);
	}

	// Polylines
	public void test06() {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-18.142,
				178.431), 2));

		// Polylines are useful for marking paths and routes on the map.
		map.addPolyline(new PolylineOptions().geodesic(true)
				.add(new LatLng(-33.866, 151.195)) // Sydney
				.add(new LatLng(-18.142, 178.431)) // Fiji
				.add(new LatLng(21.291, -157.821)) // Hawaii
				.add(new LatLng(37.423, -122.091)) // Mountain View
		);
	}

	public void currentLocation() {

		map.setMyLocationEnabled(true);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();

		Log.d(GDayMaps.TAG,
				"Best: " + locationManager.getBestProvider(criteria, false));

		Location location = locationManager
				.getLastKnownLocation(locationManager.getBestProvider(criteria,
						false));

		if (location != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.getLatitude(), location.getLongitude()), 13));

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())) // Sets the center of the map to
												// location user
					.zoom(17) // Sets the zoom
					.bearing(90) // Sets the orientation of the camera to east
					.tilt(40) // Sets the tilt of the camera to 30 degrees
					.build(); // Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));

		}

		GetCurrentLocation();
	}

	private void GetCurrentLocation() {

		double[] d = getlocation();

		LatLng Share = new LatLng(d[0], d[1]);

		map.addMarker(new MarkerOptions()
				.position(Share)
				.title("Current Location")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_launcher)));

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(Share, 15));
	}

	public double[] getlocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		Location l = null;
		for (int i = 0; i < providers.size(); i++) {
			Log.d(GDayMaps.TAG, providers.get(i));
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}
		double[] gps = new double[2];

		if (l != null) {
			gps[0] = l.getLatitude();
			gps[1] = l.getLongitude();
		}
		return gps;
	}

}
