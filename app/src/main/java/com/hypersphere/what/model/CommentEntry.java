package com.hypersphere.what.model;

public class CommentEntry {
	public String authorId;
	public String projectId;
	public String text;

	public CommentEntry(String authorId, String projectId, String text) {
		this.authorId = authorId;
		this.projectId = projectId;
		this.text = text;
	}
}
