package com.hypersphere.what.views;

import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.ImageRequester;
import com.hypersphere.what.R;
import com.hypersphere.what.Utils;
import com.hypersphere.what.model.ProjectEntry;

import java.util.ArrayList;
import java.util.List;

public class ProjectCardAdapter extends RecyclerView.Adapter<ProjectCardAdapter.ProjectCardHolder> {

	private List<ProjectEntry> projectList;
	private ProjectPreviewClickListener itemClickListener;

	public ProjectCardAdapter(List<ProjectEntry> projectList, ProjectPreviewClickListener itemClickListener) {
		this.projectList = projectList;
		this.itemClickListener = itemClickListener;
	}

	public ProjectCardAdapter(ProjectPreviewClickListener itemClickListener) {
		projectList = new ArrayList<>();
		this.itemClickListener = itemClickListener;
	}

	@NonNull
	@Override
	public ProjectCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_card_new, parent, false);
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

		private ImageRequester imageRequester;

		private CardView projectCard;
		private ImageView previewImage;
		private TextView title, description, progressText;
		private AnimatedProgressBar progressBar;

		private ProjectPreviewClickListener listener;

		public ProjectCardHolder(@NonNull View itemView) {
			super(itemView);

			imageRequester = ImageRequester.getInstance();

			projectCard = itemView.findViewById(R.id.project_card);
			previewImage = itemView.findViewById(R.id.card_preview_image);
			title = itemView.findViewById(R.id.card_title_text);
			description = itemView.findViewById(R.id.card_description_text);
			progressBar = itemView.findViewById(R.id.card_progress_bar);
			progressText = itemView.findViewById(R.id.card_progress_text);

			final Pair[] pairs = new Pair[2];
			pairs[0] = new Pair<View, String>(previewImage, "project_preview_image");
			pairs[1] = new Pair<View, String>(progressBar, "project_progress_bar");
			//pairs[2] = new Pair<View, String>(title, "project_title_text");
			//pairs[3] = new Pair<View, String>(progressText, "card_progress_text");
			//pairs[4] = new Pair<View, String>(description, "project_description_text");

			projectCard.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.projectPreviewClick(project, pairs);
				}
			});
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

			// so if you have more values in progress bar it allow you to display cool animation
			progressBar.setMax(project.donationsGoal);
			progressBar.setProgress(0, false);
			progressBar.setProgress(project.donationsCollected, true);

			//holder.progressBar.setProgress((int) project.donationsCollected, true);
			progressText.setText(Utils.getProgressString(project.donationsCollected, project.donationsGoal));
			// TODO: 28.04.2020
			//imageRequester.setImageFromUrl(previewImage, project.previewImageUrl);
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
		void projectPreviewClick(ProjectEntry project, Pair[] transitionViews);
	}
}
