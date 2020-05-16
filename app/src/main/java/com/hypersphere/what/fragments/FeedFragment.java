package com.hypersphere.what.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;
import com.hypersphere.what.activities.MainActivity;
import com.hypersphere.what.activities.MenuController;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.ProjectCardAdapter;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment implements ProjectCardAdapter.ProjectPreviewClickListener {

	View mView;

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
		CloudManager.loadProjects(new CloudManager.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				if(data.size() > 0)
					emptySheet.setVisibility(View.GONE);
				else
					emptySheet.setVisibility(View.VISIBLE);

				for(ProjectEntry project : data) {
					// TODO: 12.05.2020  
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
					adapter.addProject(project);
				}
			}

			@Override
			public void onCancel() {}
		});

		MaterialButton newButton = mView.findViewById(R.id.feed_empty_sheet_new_button);
		newButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				activity.menuController.onClick();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						activity.menuCreateButton.callOnClick();
					}
				}, MenuController.ANIMATION_DURATION);
			}
		});

		return mView;
	}

	@Override
	public void projectPreviewClick(ProjectEntry project, Pair[] transitionViews) {
		// TODO: 12.05.2020
	}
}
