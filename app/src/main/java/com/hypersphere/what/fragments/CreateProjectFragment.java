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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.activities.LocationSelectActivity;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.helpers.KeyboardHelper;
import com.hypersphere.what.helpers.MediaHelper;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.adapters.EditableGalleryRecyclerAdapter;

import java.io.File;
import java.util.List;

/**
 * Fragment with ui to create new project.
 * Has RecyclerView with {@link EditableGalleryRecyclerAdapter} to manage images.
 * Launch {@link LocationSelectActivity} on location field click to get location by user.
 */
public class CreateProjectFragment extends Fragment {

	private static final int REQUEST_LOCATION = 854;
	private boolean locationRequested = false;

	private View mView;
	private EditableGalleryRecyclerAdapter galleryAdapter;
	private FloatingActionButton acceptButton;
	private TextInputEditText locationInput;
	private TextInputEditText titleInput;
	private TextInputEditText descriptionInput;
	private TextInputEditText moneyGoalInput;
	private TextInputEditText moneyInvestInput;
	private TextInputEditText moneyWalletInput;
	private double longitude, latitude;

	public CreateProjectFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_create_project, container, false);

		RecyclerView imageRecycler = mView.findViewById(R.id.image_recycler);
		imageRecycler.hasFixedSize();
		imageRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

		galleryAdapter = new EditableGalleryRecyclerAdapter((OnResultCallbackActivity) getActivity());
		imageRecycler.setAdapter(galleryAdapter);

		acceptButton = mView.findViewById(R.id.accept_button);
		titleInput = mView.findViewById(R.id.title_edit_text);
		descriptionInput = mView.findViewById(R.id.description_edit_text);
		moneyGoalInput = mView.findViewById(R.id.money_goal_edit_text);
		moneyInvestInput = mView.findViewById(R.id.money_investment_edit_text);
		moneyWalletInput = mView.findViewById(R.id.money_wallet_edit_text);
		locationInput = mView.findViewById(R.id.location_edit_text);
		locationInput.setOnClickListener(v -> {
			if (locationRequested)
				return;
			locationRequested = true;

			locationInput.clearFocus();

			Intent intent = new Intent(getActivity(), LocationSelectActivity.class);
			if (locationInput.getText().length() > 0) {
				intent.putExtra("lastAddress", locationInput.getText().toString());
				intent.putExtra("lat", latitude);
				intent.putExtra("lon", longitude);
			}

			startActivityForResult(intent, REQUEST_LOCATION);
		});

		KeyboardHelper.setEventListener(
				getActivity(),
				isOpen -> {
					if (isOpen)
						acceptButton.hide();
					else
						acceptButton.show();
				});

		mView.findViewById(R.id.accept_button).setOnClickListener(v -> {
			ProjectEntry project = new ProjectEntry(
					"",
					titleInput.getText().toString(),
					descriptionInput.getText().toString(),
					Double.parseDouble(moneyGoalInput.getText().toString()),
					Double.parseDouble(moneyInvestInput.getText().toString()),
					latitude,
					longitude,
					null,
					CloudHelper.getCurUser().id,
					moneyWalletInput.getText().toString()
			);

			/*
			final Dialog loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Translucent);
			loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			loadingDialog.setCancelable(false);
			loadingDialog.setContentView(R.layout.loading_dialog_layout);
			ImageView imageView = loadingDialog.findViewById(R.id.loading_image_view);
			Glide.with(getContext())
					.load(R.drawable.loading_drawable)
					.placeholder(R.drawable.loading_drawable)
					.fitCenter()
					.into(imageView);

			loadingDialog.show();
			*/
			CloudHelper.newProject(project, galleryAdapter.getImages());
		});

		//force to save state
		setRetainInstance(true);
		//restoring
		if (savedInstanceState != null) {
			titleInput.setText(savedInstanceState.getString("title"));
			descriptionInput.setText(savedInstanceState.getString("description"));
			moneyGoalInput.setText(savedInstanceState.getString("moneyGoal"));
			moneyInvestInput.setText(savedInstanceState.getString("moneyInvest"));
			moneyWalletInput.setText(savedInstanceState.getString("moneyWallet"));
			locationInput.setText(savedInstanceState.getString("locationAddress"));
			latitude = savedInstanceState.getDouble("locationLatitude");
			longitude = savedInstanceState.getDouble("locationLongitude");

			int savedImagesCount = savedInstanceState.getInt("imagesCount");
			for (int i = 0; i < savedImagesCount; i++) {
				File imageFile = new File(getContext().getCacheDir(), "gallery_image_" + i + ".tmp");
				Bitmap image = MediaHelper.readBitmapFromFile(imageFile);
				galleryAdapter.addImage(image);
			}
		}

		return mView;
	}

	/**
	 * Saves state of fields.
	 * Images from gallery recycler saves as "gallery_image_" + index + ".tmp"
	 *
	 * @param outState
	 */
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("title", titleInput.getText().toString());
		outState.putString("description", descriptionInput.getText().toString());
		outState.putString("moneyGoal", moneyGoalInput.getText().toString());
		outState.putString("moneyInvest", moneyInvestInput.getText().toString());
		outState.putString("moneyWallet", moneyWalletInput.getText().toString());
		outState.putString("locationAddress", locationInput.getText().toString());
		outState.putDouble("locationLatitude", latitude);
		outState.putDouble("locationLongitude", longitude);

		List<Bitmap> imagesToSave = galleryAdapter.getImages();
		outState.putInt("imagesCount", imagesToSave.size());
		for (int i = 0; i < imagesToSave.size(); i++) {
			File imageFile = new File(getContext().getCacheDir(), "gallery_image_" + i + ".tmp");
			MediaHelper.saveBitmapToFile(imagesToSave.get(i), imageFile);
		}
	}

	/**
	 * Grabs result from {@link LocationSelectActivity}.
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data        contains String Address and double latitude and longitude if resultCode is OK.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_LOCATION) {
			if (resultCode == Activity.RESULT_OK) {
				locationInput.setText(data.getStringExtra("address"));
				latitude = data.getDoubleExtra("lat", 0);
				longitude = data.getDoubleExtra("lon", 0);
			}
			locationRequested = false;
		}
	}
}
