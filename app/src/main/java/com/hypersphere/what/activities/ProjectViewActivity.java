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

package com.hypersphere.what.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hypersphere.what.R;
import com.hypersphere.what.fragments.ProjectInfoFragment;
import com.hypersphere.what.model.ProjectEntry;

/**
 * Shows project info fragment. Starts on click on project card in feed fragment or
 * other recycler-views.
 */
public class ProjectViewActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_view);

		ProjectInfoFragment projectFragment = new ProjectInfoFragment();


		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.project_activity_frame_layout, projectFragment)
				.commit();

		ProjectEntry project = (ProjectEntry) getIntent().getExtras().getSerializable("project");

		projectFragment.fillInfoOnCreate(project);
		projectFragment.setCloseButtonVisibility(View.GONE);

		Toolbar toolbar = findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(v -> {
			onBackPressed();
		});
	}
}
