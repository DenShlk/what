package com.hypersphere.what.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;
import com.hypersphere.what.Utils;
import com.hypersphere.what.model.CommentEntry;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.AnimatedProgressBar;
import com.hypersphere.what.views.CommentAdapter;
import com.hypersphere.what.views.GalleryRecyclerAdapter;
import com.yandex.money.api.methods.payment.params.P2pTransferParams;

import java.math.BigDecimal;
import java.util.List;

import ru.yandex.money.android.PaymentActivity;


public class ProjectInfoFragment extends Fragment {

	View mView;

	private TextView infoTitle, infoMoney, infoDescription;
	private RecyclerView infoGallery;
	private GalleryRecyclerAdapter infoGalleryAdapter;
	private AnimatedProgressBar progressBar;
	private View closeButton;
	private CloseListener closeListener;
	private CommentAdapter commentAdapter;
	private MaterialButton donateButton;

	private static final String CLIENT_ID = "6BC6EB098D661CCA8771C67A3141A63588E3D6CD7E8457A9815E891B7D3CDF8F";
	private static final String HOST = "https://money.yandex.ru";
	private static final int REQUEST_CODE_YANDEX_PAYMENT = 1;

	private double payAmount = 0;
	private ProjectEntry mProject;

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
		infoGallery = mView.findViewById(R.id.info_image_recycler);

		infoGallery.setHasFixedSize(true);
		infoGallery.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		infoGalleryAdapter = new GalleryRecyclerAdapter(getActivity());
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
		addCommentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				commentInputLayout.setVisibility(View.VISIBLE);
				commentText.requestFocus();

				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(commentText, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		View sendCommentButton = mView.findViewById(R.id.info_send_comment_button);
		sendCommentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: 14.05.2020 cloudmanager work
				CommentEntry comment = new CommentEntry(CloudManager.getCurUser().id, mProject.id, commentText.getText().toString());

				CloudManager.newComment(comment);
				commentAdapter.addComment(comment);

				commentInputLayout.setVisibility(View.GONE);
				commentText.setText("");
				commentText.clearFocus();
				//hide keyboard
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mView.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});

		/*
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE |
						WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );
		 */

		ImageView userImage = mView.findViewById(R.id.info_user_image);
		CloudManager.loadImage(CloudManager.getCurUser().image, new CloudManager.OnDownloadListener<Bitmap>() {
			@Override
			public void onComplete(Bitmap data) {
				userImage.setImageBitmap(data);
			}
			@Override
			public void onCancel() {}
		});

		return mView;
	}

	public void fillInfo(final ProjectEntry project){
		mProject = project;

		infoTitle.setText(project.title);
		infoDescription.setText(project.description);
		infoMoney.setText(Utils.getProgressString(project.donationsCollected, project.donationsGoal));

		progressBar.setMax(project.donationsGoal);
		progressBar.setProgress(project.donationsCollected);

		commentAdapter.clearComments();
		CloudManager.loadComments(project.id, new CloudManager.OnDownloadListener<List<CommentEntry>>() {
			@Override
			public void onComplete(List<CommentEntry> data) {
				commentAdapter.setComments(data);
			}
			@Override
			public void onCancel() {}
		});

		infoGalleryAdapter.clear();
		for(String src : project.images){
			CloudManager.loadImage(src, new CloudManager.OnDownloadListener<Bitmap>() {
				@Override
				public void onComplete(Bitmap data) {
					infoGalleryAdapter.addImage(data);
				}

				@Override
				public void onCancel() {}
			});
		}

		donateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//debug
				//CloudManager.finishProject(project);

				View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.payment_dialog_layout, (ViewGroup) getView(), false);
				// Set up the input
				final TextInputEditText input = viewInflated.findViewById(R.id.payment_dialog_amount_input);
				input.setText(String.valueOf(project.donationsGoal - project.donationsCollected));

				new MaterialAlertDialogBuilder(getContext())
						// TODO: 12.05.2020 to dict
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						})
						.setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

								if (input.getText().toString().isEmpty()) {
									input.setError("Amount");
									return;
								}


								payAmount = Double.parseDouble(input.getText().toString());

								Intent intent = PaymentActivity.getBuilder(getContext())
										.setPaymentParams(new P2pTransferParams.Builder(project.walletId)
												.setAmount(new BigDecimal(payAmount))
												.create())
										.setClientId(CLIENT_ID)
										.setHost(HOST)
										.build();
								startActivityForResult(intent, REQUEST_CODE_YANDEX_PAYMENT);
							}
						})
						.setTitle("Donation amount")
						.setView(viewInflated)
						.show();
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_CODE_YANDEX_PAYMENT){
			if(resultCode == Activity.RESULT_OK) {
				mProject = CloudManager.notifyDonation(mProject, payAmount);
				fillInfo(mProject);
			}else
				payAmount = 0;
		}
	}

	public void setCloseListener(CloseListener listener){
		closeListener = listener;
	}

	public interface CloseListener {
		public void onClose();
	}

}
