package com.hypersphere.what.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.ProjectCardAdapter;

import java.util.List;

import static com.hypersphere.what.Utils.getProjects;

public class ProfileFragment extends Fragment {


	private RecyclerView recyclerView;

	private ProjectCardAdapter.ProjectPreviewClickListener projectPreviewClickListener;

	private View mView;

	public ProfileFragment(ProjectCardAdapter.ProjectPreviewClickListener listener) {
		projectPreviewClickListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_profile, container, false);

		recyclerView = mView.findViewById(R.id.profile_recycler_view);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		//GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
		recyclerView.setLayoutManager(linearLayoutManager);

		final ProjectCardAdapter adapter = new ProjectCardAdapter(getProjects(0), projectPreviewClickListener);
		CloudManager.loadProjects(new CloudManager.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				for(ProjectEntry project : data)
					adapter.addProject(project);
			}

			@Override
			public void onCancel() {}
		});
		recyclerView.setAdapter(adapter);

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

		return mView;
	}

}
