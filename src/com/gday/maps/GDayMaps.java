package com.gday.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.gday.maps.models.PlaceModel;
import com.gday.maps.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GDayMaps extends FragmentActivity implements OnClickListener,
		OnMarkerClickListener, LocationListener {

	private static final String URL_DATA = "http://carlospinan.com/data.json";

	private enum MAPS {
		MAP_TYPE_NORMAL, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID, MAP_TYPE_NONE
	}

	public static final String TAG = "Debug";
	private GoogleMap map;
	private Marker markerLocation;
	private boolean local_loading = true;
	private MAPS currentMap = MAPS.MAP_TYPE_NORMAL;

	private DownloadRouteTask routeTask = null;
	private List<Polyline> polyList = new ArrayList<Polyline>();

	public static Map<Marker, Bitmap> hashBitmaps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Thread.currentThread().setContextClassLoader(
				getClass().getClassLoader());

		if (savedInstanceState == null) {

			int status = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(getBaseContext());

			if (status != ConnectionResult.SUCCESS) {
				Log.d(TAG, "No se puso conectar a Google Play Services");
				return;
			}

			hashBitmaps = new HashMap<Marker, Bitmap>();

			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			if (map == null)
				return;

			_init();

		}
	}

	private void _init() {

		((Button) findViewById(R.id.btnNextMap)).setOnClickListener(this);

		map.setMyLocationEnabled(true);

		// map.getUiSettings().setZoomControlsEnabled(true);
		// map.getUiSettings().setCompassEnabled(true);
		// map.getUiSettings().setMyLocationButtonEnabled(true);
		// map.getUiSettings().setRotateGesturesEnabled(true);
		// map.getUiSettings().setTiltGesturesEnabled(true);
		// map.getUiSettings().setZoomGesturesEnabled(true);
		// map.getUiSettings().setAllGesturesEnabled(true);
		// map.setTrafficEnabled(true);

		map.setOnMarkerClickListener(this);
		map.setInfoWindowAdapter(new CustomWindowsAdapter(GDayMaps.this));

		_setMap();

		_loadCurrentLocation();

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);

		if (location != null) {
			onLocationChanged(location);
		}

		locationManager.requestLocationUpdates(provider, 20000, 0, this);

		_loadPlaces();

	}

	private void _setMap() {
		if (currentMap == MAPS.MAP_TYPE_NORMAL) {

			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		} else if (currentMap == MAPS.MAP_TYPE_HYBRID) {

			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		} else if (currentMap == MAPS.MAP_TYPE_TERRAIN) {

			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

		} else {

			map.setMapType(GoogleMap.MAP_TYPE_NONE);

		}
	}

	private void _loadCurrentLocation() {

		double last_location[] = getLastLocation();
		LatLng current = new LatLng(last_location[0], last_location[1]);

		MarkerOptions option = new MarkerOptions().position(current)
				.title("Estas aqu’")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

		markerLocation = map.addMarker(option);

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

	}

	private void _loadPlaces() {

		if (local_loading) {

			Log.d(TAG, "Local loading");

			String result = Utils.readRawTextFile(GDayMaps.this, R.raw.data);
			if (result != null)
				_load(result);

		} else {

			new DownloadContentTask().execute();

		}
	}

	private void _load(String result) {

		// Log.d(TAG, result);

		try {

			List<PlaceModel> placesList = new ArrayList<PlaceModel>();

			JSONObject object = new JSONObject(result);

			if (object != null) {

				JSONArray data = object.getJSONArray("data");
				int i;

				for (i = 0; i < data.length(); i++) {

					JSONObject currentObject = data.getJSONObject(i);
					String name = currentObject.getString("lugar");
					double latitude = currentObject.getDouble("lat");
					double longitude = currentObject.getDouble("long");
					String path = currentObject.getString("imagen");

					placesList.add(new PlaceModel(name, latitude, longitude,
							path));

				}

				if (!placesList.isEmpty()) {

					Marker m = null;

					for (i = 0; i < placesList.size(); i++) {
						PlaceModel p = placesList.get(i);

						MarkerOptions options = new MarkerOptions()
								.position(new LatLng(p.latitude, p.longitude))
								.title(p.name)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.pin));

						m = map.addMarker(options);

						Log.d(TAG, "Cargando imagen: " + p.path);
						new DownloadImageTask(m, p.path).execute();

					}

				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public double[] getLastLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		Location l = null;
		for (int i = 0; i < providers.size(); i++) {
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

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnNextMap) {

			if (currentMap == MAPS.MAP_TYPE_NONE)
				currentMap = MAPS.MAP_TYPE_NORMAL;
			else if (currentMap == MAPS.MAP_TYPE_NORMAL)
				currentMap = MAPS.MAP_TYPE_TERRAIN;
			else if (currentMap == MAPS.MAP_TYPE_TERRAIN)
				currentMap = MAPS.MAP_TYPE_HYBRID;
			else
				currentMap = MAPS.MAP_TYPE_NONE;
			_setMap();
		}

	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		if (markerLocation != marker) {
			Log.d(TAG, "Click " + marker.getTitle());

			if (routeTask != null && routeTask.getStatus() == Status.RUNNING) {
				routeTask.cancel(true);
				routeTask = null;
			}

			String url = Utils.getRouteURL(
					markerLocation.getPosition().latitude,
					markerLocation.getPosition().longitude,
					marker.getPosition().latitude,
					marker.getPosition().longitude);

			routeTask = new DownloadRouteTask(url);

		}

		return false;
	}

	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}

	// Download Images
	private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

		private String path;
		private Marker marker;

		public DownloadImageTask(Marker marker, String path) {
			this.path = path;
			this.marker = marker;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			return Utils.getBitmapFromURL(path);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null)
				hashBitmaps.put(marker, result);
		}

	}

	// Download content
	private class DownloadContentTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			return Utils.httpRequest(URL_DATA);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "Carga de contenido");
			if (result != null)
				_load(result);

		}

	}

	private class DownloadRouteTask extends AsyncTask<Void, Void, String> {

		private String url;

		public DownloadRouteTask(String url) {
			Log.d(TAG, url);
			this.url = url;
		}

		@Override
		protected String doInBackground(Void... params) {
			Log.d(TAG, "Executing bg");
			return Utils.httpRequest(url);
		}

		@Override
		protected void onPostExecute(String result) {
			routeTask = null;
			Log.d(TAG, "Cargando ruta");
			if (result != null) {

				Log.d(TAG, result);

				try {
					final JSONObject json = new JSONObject(result);
					JSONArray routeArray = json.getJSONArray("routes");
					JSONObject routes = routeArray.getJSONObject(0);
					JSONObject overviewPolylines = routes
							.getJSONObject("overview_polyline");
					String encodedString = overviewPolylines
							.getString("points");
					List<LatLng> list = decodePoly(encodedString);

					for (int i = 0; i < polyList.size(); i++)
						polyList.get(i).remove();
					polyList.clear();

					for (int z = 0; z < list.size() - 1; z++) {
						LatLng src = list.get(z);
						LatLng dest = list.get(z + 1);
						Polyline line = map.addPolyline(new PolylineOptions()
								.add(new LatLng(src.latitude, src.longitude),
										new LatLng(dest.latitude,
												dest.longitude)).width(2)
								.color(Color.BLUE).geodesic(true));
						polyList.add(line);
					}

				} catch (JSONException e) {

				}

			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

		double mLatitude = location.getLatitude();
		double mLongitude = location.getLongitude();
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		if (markerLocation != null) {
			markerLocation.setPosition(latLng);
		}

		// map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		// map.animateCamera(CameraUpdateFactory.zoomTo(12));

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
