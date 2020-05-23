package com.hypersphere.what.views;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;
import com.hypersphere.what.model.ProjectEntry;

import java.util.ArrayList;
import java.util.List;

public class ProjectCardAdapter extends RecyclerView.Adapter<ProjectCardAdapter.ProjectCardHolder> {

	private final List<ProjectEntry> projectList;
	private final ProjectPreviewClickListener itemClickListener;

	public ProjectCardAdapter(ProjectPreviewClickListener itemClickListener) {
		projectList = new ArrayList<>();
		this.itemClickListener = itemClickListener;
	}

	@NonNull
	@Override
	public ProjectCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_card, parent, false);
		return new ProjectCardHolder(layoutView);
	}

	public void addProject(ProjectEntry project){
		projectList.add(project);
		notifyItemInserted(projectList.size()-1);
	}

	@Override
	public void onBindViewHolder(@NonNull ProjectCardHolder holder, int position) {
		holder.setProject(projectList.get(position));
		holder.setListener(itemClickListener);
	}

	@Override
	public void onViewAttachedToWindow(@NonNull ProjectCardHolder holder) {
		super.onViewAttachedToWindow(holder);
		holder.reloadProject();
	}

	@Override
	public int getItemCount() {
		return projectList.size();
	}

	public class ProjectCardHolder extends RecyclerView.ViewHolder {

		ProjectEntry project;

		private final CardView projectCard;
		private final ImageView previewImage;
		private final TextView title;
		private final TextView description;
		private final TextView progressText;
		private final AnimatedProgressBar progressBar;

		private ProjectPreviewClickListener listener;

		public ProjectCardHolder(@NonNull View itemView) {
			super(itemView);

			projectCard = itemView.findViewById(R.id.project_card);
			previewImage = itemView.findViewById(R.id.card_preview_image);
			title = itemView.findViewById(R.id.card_title_text);
			description = itemView.findViewById(R.id.card_description_text);
			progressBar = itemView.findViewById(R.id.card_progress_bar);
			progressText = itemView.findViewById(R.id.card_progress_text);

			projectCard.setOnClickListener(v -> listener.projectPreviewClick(project));
		}

		public void reloadProject(){
			if(project != null)
				setProject(project);
		}

		public void setListener(ProjectPreviewClickListener listener) {
			this.listener = listener;
		}

		void setProject(ProjectEntry project) {
			this.project = project;

			title.setText(project.title);
			description.setText(project.description);

			progressBar.setMax(project.donationsGoal);
			progressBar.setProgress(project.donationsCollected, true);

			progressText.setText(ProjectEntry.getProgressString(project.donationsCollected, project.donationsGoal));

			CloudManager.loadImage(project.images.get(0), new CloudManager.OnDownloadListener<Bitmap>() {
				@Override
				public void onComplete(Bitmap data) {
					previewImage.setImageBitmap(data);
				}

				@Override
				public void onCancel() {}
			});
		}


	}

	public interface ProjectPreviewClickListener{
		void projectPreviewClick(ProjectEntry project);
	}
}
