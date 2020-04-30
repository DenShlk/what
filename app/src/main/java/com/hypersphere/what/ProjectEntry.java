package com.hypersphere.what;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

public class ProjectEntry implements Serializable {
	// TODO: 05.04.2020

	public String title;
	public String description;
	public double donationsGoal;
	public double donationsCollected;

	public double latitude, longitude;

	public List<String> images;

	public ProjectEntry(String title, String description, double donationsGoal, double donationsCollected, double latitude, double longitude, List<String> images) {
		this.title = title;
		this.description = description;
		this.donationsGoal = donationsGoal;
		this.donationsCollected = donationsCollected;
		this.latitude = latitude;
		this.longitude = longitude;
		this.images = images;
	}

	public String getJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
