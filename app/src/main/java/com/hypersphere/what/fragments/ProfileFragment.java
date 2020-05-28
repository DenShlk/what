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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.activities.ProjectViewActivity;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.helpers.MediaHelper;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.adapters.ProjectCardAdapter;

import java.util.List;

/**
 * Shows current user's profile information.
 * Has two recycler views for active and finished projects of current user.
 * Recyclers uses {@link PagerSnapHelper}.
 */
public class ProfileFragment extends Fragment implements OnResultCallbackActivity.ActivityResultListener {

	private static final int REQUEST_IMAGE_CAPTURE = 5413;
	private static final int REQUEST_IMAGE_FROM_GALLERY = 2434;

	private final ProjectCardAdapter.ProjectPreviewClickListener projectPreviewClickListener;

	private View mView;
	private ImageView userImageView;

	public ProfileFragment() {
		projectPreviewClickListener = project -> {
			Intent intent = new Intent(getActivity(), ProjectViewActivity.class);
			intent.putExtra("project", project);
			startActivity(intent);
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

		final ProjectCardAdapter activeAdapter = new ProjectCardAdapter(projectPreviewClickListener);
		for (String projectId : CloudHelper.getCurUser().myProjects) {
			CloudHelper.loadProject(projectId, new CloudHelper.OnDownloadListener<ProjectEntry>() {
				@Override
				public void onComplete(ProjectEntry data) {
					activeAdapter.addProject(data);
				}

				@Override
				public void onCancel() {}
			});
		}
		if (CloudHelper.getCurUser().myProjects.size() == 0) {
			TextView text = mView.findViewById(R.id.profile_my_active_projects_text);
			text.setText(getResources().getString(R.string.profile_no_active_projects));
			activeRecycler.setVisibility(View.INVISIBLE);
		}
		activeRecycler.setAdapter(activeAdapter);

		RecyclerView finishedRecycler = mView.findViewById(R.id.profile_finished_recycler);
		LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		finishedRecycler.setLayoutManager(linearLayoutManager2);
		SnapHelper pagerSnap2 = new PagerSnapHelper();
		pagerSnap2.attachToRecyclerView(finishedRecycler);

		final ProjectCardAdapter finishedAdapter = new ProjectCardAdapter(projectPreviewClickListener);
		CloudHelper.loadDoneProjects(new CloudHelper.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				for(ProjectEntry project : data)
					finishedAdapter.addProject(project);
				if(data.size() == 0){
					TextView text = mView.findViewById(R.id.profile_my_finished_projects_text);
					text.setText(getResources().getString(R.string.profile_no_finished_projects));
					finishedRecycler.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onCancel() {}
		});
		finishedRecycler.setAdapter(finishedAdapter);

		userImageView = mView.findViewById(R.id.user_image_view);
		CloudHelper.loadImage(CloudHelper.getCurUser().image, new CloudHelper.OnDownloadListener<Bitmap>() {
			@Override
			public void onComplete(Bitmap data) {
				userImageView.setImageBitmap(data);
			}

			@Override
			public void onCancel() {}
		});
		//Open bottom dialog to change current user avatar.
		userImageView.setOnClickListener(v -> {

			final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
			View sheetView = getActivity().getLayoutInflater().inflate(R.layout.change_avatar_dialog_layout, null);
			mBottomSheetDialog.setContentView(sheetView);
			mBottomSheetDialog.show();

			View takeButton = sheetView.findViewById(R.id.dialog_take_photo);
			takeButton.setOnClickListener(v17 -> {
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				getActivity().startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);

				mBottomSheetDialog.cancel();
			});

			View galleryButton = sheetView.findViewById(R.id.dialog_open_gallery);
			galleryButton.setOnClickListener(v16 -> {
				Intent galleryIntent = new Intent(Intent.ACTION_PICK);
				galleryIntent.setType("image/*");
				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
				getActivity().startActivityForResult(galleryIntent, REQUEST_IMAGE_FROM_GALLERY);

				mBottomSheetDialog.cancel();
			});

		});

		TextView nickText = mView.findViewById(R.id.profile_nick_text);
		nickText.setText(CloudHelper.getCurUser().username);

		TextView emailText = mView.findViewById(R.id.profile_email_text);
		emailText.setText(CloudHelper.getCurUser().email);

		((OnResultCallbackActivity) getActivity()).addActivityResultListener(ProfileFragment.this);

		return mView;
	}

	/**
	 * Grabs images to replace current user avatar.
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE_CAPTURE) {

			if (resultCode == Activity.RESULT_OK) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				userImageView.setImageBitmap(photo);
				CloudHelper.setUserImage(photo);
			}
		}
		if (requestCode == REQUEST_IMAGE_FROM_GALLERY) {

			if (resultCode == Activity.RESULT_OK) {
				Uri photoUri = data.getData();
				Bitmap photo = MediaHelper.getImageByUri(photoUri);
				if (photo != null) {
					userImageView.setImageBitmap(photo);
					CloudHelper.setUserImage(photo);
				}
			}
		}
	}
}
