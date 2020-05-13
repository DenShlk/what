package com.hypersphere.what.model;

import java.util.List;

public class UserEntry {
	public String email;
	public String username;
	public List<String> myProjects;
	public String id;
	public String image;

	public UserEntry(String email, String username, List<String> myProjects, String id, String image) {
		this.email = email;
		this.username = username;
		this.myProjects = myProjects;
		this.id = id;
		this.image = image;
	}
}
