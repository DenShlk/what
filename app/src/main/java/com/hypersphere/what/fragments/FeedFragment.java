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

package com.hypersphere.what.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.R;
import com.hypersphere.what.activities.MainActivity;
import com.hypersphere.what.activities.ProjectViewActivity;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.adapters.ProjectCardAdapter;

import java.util.List;
/**
 * Shows a list of projects to user. Starts {@link ProjectViewActivity} on project card click.
 * If there aren't any projects shows empty sheet.
 * Implements {@link ProjectCardAdapter.ProjectPreviewClickListener}
 * to listen clicks on project cards.
 */
public class FeedFragment extends Fragment implements ProjectCardAdapter.ProjectPreviewClickListener {

	private View mView;

	public FeedFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_feed, container, false);

		View emptySheet = mView.findViewById(R.id.feed_empty_sheet);

		RecyclerView recycler = mView.findViewById(R.id.feed_recycler_view);
		recycler.setLayoutManager(new LinearLayoutManager(getContext()));
		final ProjectCardAdapter adapter = new ProjectCardAdapter(FeedFragment.this);
		recycler.setAdapter(adapter);
		CloudHelper.loadProjects(new CloudHelper.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				if(data.size() > 0)
					emptySheet.setVisibility(View.GONE);
				else
					emptySheet.setVisibility(View.VISIBLE);

				for(ProjectEntry project : data) {
					adapter.addProject(project);
				}
			}

			@Override
			public void onCancel() {}
		});

		//Empty sheet button, navigates to CreateProject fragment on click
		MaterialButton newButton = mView.findViewById(R.id.feed_empty_sheet_new_button);
		newButton.setOnClickListener(v -> {
			MainActivity activity = (MainActivity) getActivity();
			activity.smoothNavigateTo(MainActivity.MainFragmentsEnum.CreateProject);
		});

		return mView;
	}

	/**
	 * Starts {@link ProjectViewActivity} on click on project card.
	 *
	 * @param project
	 */
	@Override
	public void projectPreviewClick(ProjectEntry project) {
		Intent intent = new Intent(getActivity(), ProjectViewActivity.class);
		intent.putExtra("project", project);
		startActivity(intent);
	}
}
