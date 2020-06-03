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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.activities.LocationSelectActivity;
import com.hypersphere.what.activities.MainActivity;
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
	private TextInputLayout locationInput;
	private TextInputLayout titleInput;
	private TextInputLayout descriptionInput;
	private TextInputLayout moneyGoalInput;
	private TextInputLayout moneyInvestInput;
	private TextInputLayout moneyWalletInput;
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
		titleInput = mView.findViewById(R.id.title_edit_layout);
		descriptionInput = mView.findViewById(R.id.description_edit_layout);
		moneyGoalInput = mView.findViewById(R.id.money_goal_edit_layout);
		moneyInvestInput = mView.findViewById(R.id.money_investment_edit_layout);
		moneyWalletInput = mView.findViewById(R.id.money_wallet_edit_layout);
		locationInput = mView.findViewById(R.id.location_edit_layout);
		locationInput.setOnClickListener(v -> {
			if (locationRequested)
				return;
			locationRequested = true;

			locationInput.clearFocus();

			Intent intent = new Intent(getActivity(), LocationSelectActivity.class);
			if (locationInput.getEditText().getText().length() > 0) {
				intent.putExtra("lastAddress", locationInput.getEditText().getText().toString());
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

		View acceptButton = mView.findViewById(R.id.accept_button);
		acceptButton.setOnClickListener(v -> {
			if (isInputCorrect()) {
				ProjectEntry project = new ProjectEntry(
						"",
						titleInput.getEditText().getText().toString(),
						descriptionInput.getEditText().getText().toString(),
						Double.parseDouble(moneyGoalInput.getEditText().getText().toString()),
						Double.parseDouble(moneyInvestInput.getEditText().getText().toString()),
						latitude,
						longitude,
						null,
						CloudHelper.getCurUser().id,
						moneyWalletInput.getEditText().getText().toString()
				);

				CloudHelper.newProject(project, galleryAdapter.getImages());

				MainActivity activity = (MainActivity) getActivity();
				activity.smoothNavigateTo(MainActivity.MainFragmentsEnum.Map);

				clearFields();
			}
		});

		//force to save state
		setRetainInstance(true);
		//restoring
		if (savedInstanceState != null) {
			titleInput.getEditText().setText(savedInstanceState.getString("title"));
			descriptionInput.getEditText().setText(savedInstanceState.getString("description"));
			moneyGoalInput.getEditText().setText(savedInstanceState.getString("moneyGoal"));
			moneyInvestInput.getEditText().setText(savedInstanceState.getString("moneyInvest"));
			moneyWalletInput.getEditText().setText(savedInstanceState.getString("moneyWallet"));
			locationInput.getEditText().setText(savedInstanceState.getString("locationAddress"));
			latitude = savedInstanceState.getDouble("locationLatitude");
			longitude = savedInstanceState.getDouble("locationLongitude");

			int savedImagesCount = savedInstanceState.getInt("imagesCount");
			for (int i = 0; i < savedImagesCount; i++) {
				File imageFile = new File(getContext().getCacheDir(), "gallery_image_" + i + ".tmp");
				Bitmap image = MediaHelper.readBitmapFromFile(imageFile);
				galleryAdapter.addImage(image);
			}
		}

		setInputErrorMessages();

		return mView;
	}

	/**
	 * Adds {@link android.widget.TextView.OnEditorActionListener} to input fields with
	 * input-correctness check.
	 */
	private void setInputErrorMessages() {
		//title check (>3 chars)
		titleInput.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			if (titleInput.getEditText().getText().toString().isEmpty())
				titleInput.setError(getString(R.string.create_error_at_least_3));
			else
				titleInput.setError(null);
			return false;
		});

		//description can be empty (no checks)

		//goal > invest
		TextView.OnEditorActionListener moneyInputsChecker = (v, actionId, event) -> {
			moneyInvestInput.setError(null);
			moneyGoalInput.setError(null);

			double investAmount = -1, goalAmount = -1;
			try {
				investAmount = Double.parseDouble(moneyInvestInput.getEditText().getText().toString());
			} catch (NumberFormatException e) {
				moneyInvestInput.setError(getString(R.string.create_error_fill_amount));
			}
			try {
				goalAmount = Double.parseDouble(moneyGoalInput.getEditText().getText().toString());
			} catch (NumberFormatException e) {
				moneyGoalInput.setError(getString(R.string.create_error_fill_amount));
			}

			if (investAmount == -1 || goalAmount == -1)
				return false;

			if (goalAmount <= investAmount) {
				moneyInvestInput.setError(getString(R.string.create_error_goal_bigger_invest));
				moneyGoalInput.setError(getString(R.string.create_error_invest_smaller_goal));
			}

			return false;
		};
		moneyInvestInput.getEditText().setOnEditorActionListener(moneyInputsChecker);
		moneyGoalInput.getEditText().setOnEditorActionListener(moneyInputsChecker);

		//wallet id is 15 digits
		moneyWalletInput.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			if (moneyWalletInput.getEditText().getText().length() != 15) {
				moneyWalletInput.setError(getString(R.string.create_error_15_digits));
			} else moneyWalletInput.setError(null);
			return false;
		});
	}

	/**
	 * Checks is all fields of project is correct.
	 * Firstly checks errors in text fields, then checks that location is entered and finally that
	 * at gallery contains at least 1 image.
	 *
	 * @return
	 */
	private boolean isInputCorrect() {
		boolean correct = true;

		TextInputLayout[] textInputs = {titleInput, descriptionInput, moneyGoalInput, moneyInvestInput, moneyWalletInput};
		for (TextInputLayout input : textInputs) {
			if (input.getError() != null && !input.getError().toString().isEmpty()) correct = false;
		}

		//address don't entered
		if (longitude == 0 || latitude == 0) {
			locationInput.setError("Enter location");
			correct = false;
		}

		if (galleryAdapter.getImages().size() == 0) {
			Toast.makeText(getContext(), "You should add image", Toast.LENGTH_LONG).show();
			correct = false;
		}
		return correct;
	}

	/**
	 * Clears oll input data.
	 */
	private void clearFields() {
		locationInput.getEditText().setText("");
		titleInput.getEditText().setText("");
		descriptionInput.getEditText().setText("");
		moneyGoalInput.getEditText().setText("");
		moneyInvestInput.getEditText().setText("");
		moneyWalletInput.getEditText().setText("");
		latitude = 0;
		longitude = 0;
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

		outState.putString("title", titleInput.getEditText().getText().toString());
		outState.putString("description", descriptionInput.getEditText().getText().toString());
		outState.putString("moneyGoal", moneyGoalInput.getEditText().getText().toString());
		outState.putString("moneyInvest", moneyInvestInput.getEditText().getText().toString());
		outState.putString("moneyWallet", moneyWalletInput.getEditText().getText().toString());
		outState.putString("locationAddress", locationInput.getEditText().getText().toString());
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
	 * Grabs result from {@link LocationSelectActivity}. If location is gotten clears
	 * locationInput's error.
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
				locationInput.getEditText().setText(data.getStringExtra("address"));
				latitude = data.getDoubleExtra("lat", 0);
				longitude = data.getDoubleExtra("lon", 0);
				locationInput.setError(null);
			}
			locationRequested = false;
		}
	}
}
