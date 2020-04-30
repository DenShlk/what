package views;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;
import com.hypersphere.what.ImageRequester;
import com.hypersphere.what.ProjectEntry;
import com.hypersphere.what.R;
import com.hypersphere.what.Utils;

public class ProjectCardViewHolder extends RecyclerView.ViewHolder {

	ProjectEntry project;

	private ImageRequester imageRequester;

	private CardView projectCard;
	private NetworkImageView previewImage;
	private TextView title, description, progressText;
	private AnimatedProgressBar progressBar;

	private ProjectPreviewClickListener listener;

	public ProjectCardViewHolder(@NonNull View itemView) {
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

	}

	public interface ProjectPreviewClickListener{
		void projectPreviewClick(ProjectEntry project, Pair[] transitionViews);
	}
}
