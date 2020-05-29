/*
 * Copyright 2020 Denis Shulakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hypersphere.what.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Presents data of project.
 */
public class ProjectEntry implements Serializable {

	public String id;
	public String title;
	public String description;
	public double donationsGoal;
	public double donationsCollected;

	public double latitude, longitude;

	public List<String> images;

	public String creatorId;

	public String walletId;

	public ProjectEntry(String id, String title, String description, double donationsGoal, double donationsCollected, double latitude, double longitude, List<String> images, String creatorId, String walletId) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.donationsGoal = donationsGoal;
		this.donationsCollected = donationsCollected;
		this.latitude = latitude;
		this.longitude = longitude;
		this.images = images;
		this.creatorId = creatorId;
		this.walletId = walletId;
	}

	public static String getProgressString(double value, double goal) {
		DecimalFormat format = new DecimalFormat("#.##");
		return format.format(value) + "/" + format.format(goal) + "\u20BD";
	}
}
