package com.gday.maps.models;

public class PlaceModel {

	public String name;
	public double latitude;
	public double longitude;
	public String path;

	public PlaceModel(String name, double latitude, double longitude,
			String path) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.path = path;
	}

}
