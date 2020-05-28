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

/**
 * Presents data of comment.
 */
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
