package com.gday.maps.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils {

	public static String readRawTextFile(Context context, int resource) {

		try {
			InputStream is = context.getResources().openRawResource(resource);
			byte[] data = new byte[is.available()];
			is.read(data);
			return new String(data, Charset.forName("UTF-8"));

		} catch (Exception e) {
			return null;
		}

	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			java.net.URL url = new java.net.URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String httpRequest(String url) {

		String response = null;

		HttpGet http_get = new HttpGet(url);

		HttpParams httpParameters = new BasicHttpParams();

		HttpClient http_client = new DefaultHttpClient(httpParameters);

		try {
			HttpResponse http_response = http_client.execute(http_get);
			if (http_response.getStatusLine().getStatusCode() == 200) {
				response = EntityUtils.toString(http_response.getEntity(),
						HTTP.UTF_8).trim();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;

	}

	public static String getRouteURL(double sourcelat, double sourcelog,
			double destlat, double destlog) {
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");// from
		urlString.append(Double.toString(sourcelat));
		urlString.append(",");
		urlString.append(Double.toString(sourcelog));
		urlString.append("&destination=");// to
		urlString.append(Double.toString(destlat));
		urlString.append(",");
		urlString.append(Double.toString(destlog));
		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}
}
