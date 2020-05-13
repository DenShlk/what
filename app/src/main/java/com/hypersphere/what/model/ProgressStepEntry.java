package com.hypersphere.what.model;

public class ProgressStepEntry {
	public boolean complete;
	public String illustration;
	public String title, description, date;

	public ProgressStepEntry(boolean complete, String illustration, String title, String description, String date) {
		this.complete = complete;
		this.illustration = illustration;
		this.title = title;
		this.description = description;
		this.date = date;
	}
}
