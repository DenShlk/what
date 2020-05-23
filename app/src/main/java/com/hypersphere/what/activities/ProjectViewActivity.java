package com.hypersphere.what.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.hypersphere.what.R;
import com.hypersphere.what.fragments.ProjectInfoFragment;
import com.hypersphere.what.model.ProjectEntry;

public class ProjectViewActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_view);

		ProjectInfoFragment projectFragment = new ProjectInfoFragment();

		FragmentTransaction transaction =
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.project_activity_frame_layout, projectFragment);

		ProjectEntry project = (ProjectEntry) getIntent().getExtras().getSerializable("project");

		projectFragment.fillInfo(project);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	// actionBar home click
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
