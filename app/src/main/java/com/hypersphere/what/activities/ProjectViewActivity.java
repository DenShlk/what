package com.hypersphere.what.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.NetworkImageView;
import com.hypersphere.what.ImageRequester;
import com.hypersphere.what.R;
import com.hypersphere.what.Utils;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.AnimatedProgressBar;

public class ProjectViewActivity extends AppCompatActivity {

	ProjectEntry project;

	private ImageRequester imageRequester;

	private NetworkImageView previewImage;
	private TextView title, description, progressText;
	private AnimatedProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_view);

		imageRequester = ImageRequester.getInstance();

		previewImage = findViewById(R.id.project_view_preview_image);
		title = findViewById(R.id.project_view_title_text);
		description = findViewById(R.id.project_view_description_text);
		progressBar = findViewById(R.id.project_view_progress_bar);
		progressText = findViewById(R.id.card_progress_text);

		setProject((ProjectEntry) getIntent().getExtras().getSerializable("project"));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	// actionBar home click
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
