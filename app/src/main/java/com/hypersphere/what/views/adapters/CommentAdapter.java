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

package com.hypersphere.what.views.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.model.CommentEntry;
import com.hypersphere.what.model.UserEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents list of comments.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

	private List<CommentEntry> comments = new ArrayList<>();

	public void setComments(List<CommentEntry> comments) {
		this.comments = comments;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
		return new CommentViewHolder(layoutView);
	}

	/**
	 * Adds new comment to top of the list.
	 *
	 * @param comment
	 */
	public void addComment(CommentEntry comment){
		comments.add(0, comment);
		notifyItemInserted(0);
	}

	@Override
	public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
		holder.setComment(comments.get(position));
	}

	@Override
	public int getItemCount() {
		return comments.size();
	}

	public void clearComments() {
		comments.clear();
		notifyDataSetChanged();
	}

	/**
	 * Holder of comment item. Shows avatar and username of author, and comment text.
	 */
	class CommentViewHolder extends RecyclerView.ViewHolder {

		private final TextView authorText;
		private final TextView commentText;
		private final ImageView authorImage;

		CommentViewHolder(@NonNull View itemView) {
			super(itemView);

			authorText = itemView.findViewById(R.id.comment_author_name);
			commentText = itemView.findViewById(R.id.comment_text);
			authorImage = itemView.findViewById(R.id.comment_author_image);
		}

		void setComment(CommentEntry comment){
			commentText.setText(comment.text);
			CloudHelper.getUser(comment.authorId, new CloudHelper.OnDownloadListener<UserEntry>() {
				@Override
				public void onComplete(UserEntry data) {
					authorText.setText(data.username);
					CloudHelper.loadImage(data.image, new CloudHelper.OnDownloadListener<Bitmap>() {
						@Override
						public void onComplete(Bitmap data) {
							authorImage.setImageBitmap(data);
						}

						@Override
						public void onCancel() {}
					});
				}

				@Override
				public void onCancel() {}
			});
		}
	}
}
