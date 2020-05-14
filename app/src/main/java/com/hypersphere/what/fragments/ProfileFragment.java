package com.hypersphere.what.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.ProjectCardAdapter;

import java.util.List;

import static com.hypersphere.what.Utils.getProjects;

public class ProfileFragment extends Fragment {


	private ProjectCardAdapter.ProjectPreviewClickListener projectPreviewClickListener;

	private View mView;

	public ProfileFragment() {
		projectPreviewClickListener = new ProjectCardAdapter.ProjectPreviewClickListener() {
			@Override
			public void projectPreviewClick(ProjectEntry project, Pair[] transitionViews) {
				// TODO: 15.05.2020 start activity
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_profile, container, false);

		RecyclerView activeRecycler = mView.findViewById(R.id.profile_active_recycler);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		activeRecycler.setLayoutManager(linearLayoutManager);
		SnapHelper pagerSnap = new PagerSnapHelper();
		pagerSnap.attachToRecyclerView(activeRecycler);

		final ProjectCardAdapter activeAdapter = new ProjectCardAdapter(getProjects(0), projectPreviewClickListener);
		CloudManager.loadProjectsDone(new CloudManager.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				for(ProjectEntry project : data)
					activeAdapter.addProject(project);
			}

			@Override
			public void onCancel() {}
		});
		activeRecycler.setAdapter(activeAdapter);

		RecyclerView finishedRecycler = mView.findViewById(R.id.profile_finished_recycler);
		LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		finishedRecycler.setLayoutManager(linearLayoutManager2);
		SnapHelper pagerSnap2 = new PagerSnapHelper();
		pagerSnap2.attachToRecyclerView(finishedRecycler);

		final ProjectCardAdapter finishedAdapter = new ProjectCardAdapter(getProjects(0), projectPreviewClickListener);
		CloudManager.loadProjectsDone(new CloudManager.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				for(ProjectEntry project : data)
					finishedAdapter.addProject(project);
			}

			@Override
			public void onCancel() {}
		});
		finishedRecycler.setAdapter(finishedAdapter);


		final ImageView userImageView = mView.findViewById(R.id.user_image_view);
		CloudManager.loadImage(CloudManager.getCurUser().image, new CloudManager.OnDownloadListener<Bitmap>() {
			@Override
			public void onComplete(Bitmap data) {
				userImageView.setImageBitmap(data);
			}

			@Override
			public void onCancel() {}
		});
		userImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: 12.05.2020 change image
			}
		});

		TextView nickText = mView.findViewById(R.id.profile_nick_text);
		nickText.setText(CloudManager.getCurUser().username);

		TextView emailText = mView.findViewById(R.id.profile_email_text);
		emailText.setText(CloudManager.getCurUser().email);

		return mView;
	}

}
