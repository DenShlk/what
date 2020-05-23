package com.hypersphere.what.views;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;
import com.hypersphere.what.model.CommentEntry;
import com.hypersphere.what.model.UserEntry;

import java.util.ArrayList;
import java.util.List;

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
			CloudManager.getUser(comment.authorId, new CloudManager.OnDownloadListener<UserEntry>() {
				@Override
				public void onComplete(UserEntry data) {
					authorText.setText(data.username);
					CloudManager.loadImage(data.image, new CloudManager.OnDownloadListener<Bitmap>() {
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
