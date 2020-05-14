package com.hypersphere.what.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.activities.LocationSelectActivity;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.EditableGalleryRecyclerAdapter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;


public class CreateProjectFragment extends Fragment {

	private static final int REQUEST_LOCATION = 854;
	private boolean locationRequested = false;

	private View mView;
	private RecyclerView imageRecycler;
	private EditableGalleryRecyclerAdapter galleryAdapter;
	private FloatingActionButton acceptButton;
	private TextInputEditText locationInput;
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

		imageRecycler = mView.findViewById(R.id.image_recycler);
		imageRecycler.hasFixedSize();
		imageRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

		galleryAdapter = new EditableGalleryRecyclerAdapter((OnResultCallbackActivity) getActivity());
		imageRecycler.setAdapter(galleryAdapter);

		acceptButton = mView.findViewById(R.id.accept_button);
		final TextInputEditText titleInput = mView.findViewById(R.id.title_edit_text);
		final TextInputEditText descriptionInput = mView.findViewById(R.id.description_edit_text);
		final TextInputEditText moneyGoalInput = mView.findViewById(R.id.money_goal_edit_text);
		final TextInputEditText moneyInvestInput = mView.findViewById(R.id.money_investment_edit_text);
		final TextInputEditText moneyWalletInput = mView.findViewById(R.id.money_wallet_edit_text);


		locationInput = mView.findViewById(R.id.location_edit_text);
		locationInput.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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
			}
		});

		KeyboardVisibilityEvent.setEventListener(
				getActivity(),
				new KeyboardVisibilityEventListener() {
					@Override
					public void onVisibilityChanged(boolean isOpen) {
						if (isOpen)
							acceptButton.hide();
						else
							acceptButton.show();
					}
				});

		mView.findViewById(R.id.accept_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ProjectEntry project = new ProjectEntry(
						"",
						titleInput.getText().toString(),
						descriptionInput.getText().toString(),
						Double.parseDouble(moneyGoalInput.getText().toString()),
						Double.parseDouble(moneyInvestInput.getText().toString()),
						latitude,
						longitude,
						null,
						CloudManager.getCurUser().id,
						moneyWalletInput.getText().toString()
				);


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

				CloudManager.newProject(project, galleryAdapter.getImages(), new CloudManager.OnUploadListener() {
					@Override
					public void onComplete() {
						loadingDialog.dismiss();
					}

					@Override
					public void onCancel() {}
				});
			}
		});

		return mView;
	}

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
