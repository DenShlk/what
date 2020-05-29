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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.hypersphere.what.R;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.helpers.KeyboardHelper;
import com.hypersphere.what.model.CommentEntry;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.AnimatedProgressBar;
import com.hypersphere.what.views.adapters.CommentAdapter;
import com.hypersphere.what.views.adapters.GalleryRecyclerAdapter;
import com.yandex.money.api.methods.payment.params.P2pTransferParams;

import java.math.BigDecimal;
import java.util.List;

import ru.yandex.money.android.PaymentActivity;

/**
 * Shows information about project, and implements payment functional.
 * On donation button click it starts {@link PaymentActivity}
 */
public class ProjectInfoFragment extends Fragment {

	View mView;

	private TextView infoTitle, infoMoney, infoDescription;
	private GalleryRecyclerAdapter infoGalleryAdapter;
	private AnimatedProgressBar progressBar;
	private CloseListener closeListener;
	private CommentAdapter commentAdapter;
	private MaterialButton donateButton;
	private View closeButton;

	private static final String CLIENT_ID = "6BC6EB098D661CCA8771C67A3141A63588E3D6CD7E8457A9815E891B7D3CDF8F";
	private static final String HOST = "https://money.yandex.ru";
	private static final int REQUEST_CODE_YANDEX_PAYMENT = 4134;

	private double payAmount = 0;
	private ProjectEntry mProject;
	private int closeButtonVisibility = View.VISIBLE;

	public ProjectInfoFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		mView =inflater.inflate(R.layout.fragment_project_info, container, false);

		infoTitle = mView.findViewById(R.id.info_title_text);
		infoMoney = mView.findViewById(R.id.info_money_text);
		infoDescription = mView.findViewById(R.id.info_description);
		RecyclerView infoGallery = mView.findViewById(R.id.info_image_recycler);

		infoGallery.setHasFixedSize(true);
		infoGallery.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		infoGalleryAdapter = new GalleryRecyclerAdapter();
		infoGallery.setAdapter(infoGalleryAdapter);

		progressBar = mView.findViewById(R.id.info_progress_bar);

		closeButton = mView.findViewById(R.id.info_close_button);
		closeButton.setOnClickListener(v -> {
			if(closeListener!=null)
				closeListener.onClose();
		});
		donateButton = mView.findViewById(R.id.info_donate_button);

		RecyclerView commentRecycler = mView.findViewById(R.id.info_comments_recycler);
		commentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
		commentAdapter = new CommentAdapter();
		commentRecycler.setAdapter(commentAdapter);
		commentRecycler.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

		TextInputEditText commentText = mView.findViewById(R.id.info_add_comment_input);

		View commentInputLayout = mView.findViewById(R.id.info_add_comment_layout);

		View addCommentButton = mView.findViewById(R.id.info_add_comment_button);
		addCommentButton.setOnClickListener(v -> {

			commentInputLayout.setVisibility(View.VISIBLE);
			commentText.requestFocus();

			KeyboardHelper.openKeyboard(commentText);
		});
		View sendCommentButton = mView.findViewById(R.id.info_send_comment_button);
		sendCommentButton.setOnClickListener(v -> {
			CommentEntry comment = new CommentEntry(CloudHelper.getCurUser().id, mProject.id, commentText.getText().toString());

			CloudHelper.newComment(comment);
			commentAdapter.addComment(comment);

			commentInputLayout.setVisibility(View.GONE);
			commentText.setText("");
			commentText.clearFocus();

			KeyboardHelper.hideKeyboard(mView);
		});

		ImageView userImage = mView.findViewById(R.id.info_user_image);
		CloudHelper.loadImage(CloudHelper.getCurUser().image, new CloudHelper.OnDownloadListener<Bitmap>() {
			@Override
			public void onComplete(Bitmap data) {
				userImage.setImageBitmap(data);
			}

			@Override
			public void onCancel() {}
		});

		if (mProject != null) fillInfo(mProject);

		setCloseButtonVisibility(closeButtonVisibility);

		return mView;
	}

	public void setCloseButtonVisibility(int visibility) {
		if (closeButton == null) {
			closeButtonVisibility = visibility;
		} else {
			closeButton.setVisibility(visibility);
		}
	}

	/**
	 * Sets mProject as given project. You should use it if you want fill project info directly
	 * after fragment creating. You can't use fillInfo(), because fragment doesn't inflated still (probably).
	 *
	 * @param project
	 */
	public void fillInfoOnCreate(ProjectEntry project) {
		mProject = project;
	}

	/**
	 * Fills info from project data to fragment views.
	 *
	 * @param project
	 */
	public void fillInfo(final ProjectEntry project){
		mProject = project;

		infoTitle.setText(project.title);
		infoDescription.setText(project.description);
		infoMoney.setText(ProjectEntry.getProgressString(project.donationsCollected, project.donationsGoal));

		progressBar.setMaxProgress(project.donationsGoal);
		progressBar.setProgress(project.donationsCollected);

		commentAdapter.clearComments();
		CloudHelper.loadComments(project.id, new CloudHelper.OnDownloadListener<List<CommentEntry>>() {
			@Override
			public void onComplete(List<CommentEntry> data) {
				commentAdapter.setComments(data);
			}

			@Override
			public void onCancel() {}
		});

		infoGalleryAdapter.clear();
		for (int i = 0; i < project.images.size(); i++) {
			int finalI = i;
			CloudHelper.loadImage(project.images.get(i), new CloudHelper.OnDownloadListener<Bitmap>() {
				final int curI = finalI;

				@Override
				public void onComplete(Bitmap data) {
					infoGalleryAdapter.addImage(data, curI);
				}

				@Override
				public void onCancel() {}
			});
		}

		donateButton.setOnClickListener(v -> {

			View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.payment_dialog_layout, (ViewGroup) getView(), false);
			// Set up the input
			final TextInputEditText input = viewInflated.findViewById(R.id.payment_dialog_amount_input);
			input.setText(String.valueOf(project.donationsGoal - project.donationsCollected));

			new MaterialAlertDialogBuilder(getContext())
					.setNegativeButton(getActivity().getString(R.string.payment_dialog_cancel), (dialog, which) -> dialog.cancel())
					.setPositiveButton(getActivity().getString(R.string.payment_dialog_continue), (dialog, which) -> {

						if (input.getText().toString().isEmpty()) {
							return;
						}

						//save pay amount to notify CloudHelper if payment is successful
						payAmount = Double.parseDouble(input.getText().toString());
						Intent intent = PaymentActivity.getBuilder(getContext())
								.setPaymentParams(new P2pTransferParams.Builder(project.walletId)
										.setAmount(new BigDecimal(payAmount))
										.create())
								.setClientId(CLIENT_ID)
								.setHost(HOST)
								.build();
						startActivityForResult(intent, REQUEST_CODE_YANDEX_PAYMENT);
					})
					.setTitle(getActivity().getString(R.string.payment_dialog_title))
					.setView(viewInflated)
					.show();
		});

	}

	/**
	 * Notifies {@link CloudHelper} and updates ui if payment was successful.
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_CODE_YANDEX_PAYMENT){
			if(resultCode == Activity.RESULT_OK) {
				CloudHelper.notifyDonation(mProject, payAmount);
				fillInfo(mProject);
			}else
				payAmount = 0;
		}
	}

	public void setCloseListener(CloseListener listener){
		closeListener = listener;
	}

	/**
	 * Interface to notify context where this fragment was created.
	 */
	public interface CloseListener {
		void onClose();
	}
}
