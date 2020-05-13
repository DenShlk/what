package com.hypersphere.what.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.ProgressCardsFragment;
import com.hypersphere.what.R;
import com.hypersphere.what.Utils;
import com.hypersphere.what.model.ProjectEntry;
import com.hypersphere.what.views.AnimatedProgressBar;
import com.hypersphere.what.views.GalleryRecyclerAdapter;
import com.yandex.money.api.methods.payment.params.P2pTransferParams;

import java.math.BigDecimal;

import ru.yandex.money.android.PaymentActivity;


public class ProjectInfoFragment extends Fragment {

	View mView;

	private TextView infoTitle, infoMoney, infoDescription;
	private RecyclerView infoGallery;
	private GalleryRecyclerAdapter infoGalleryAdapter;
	private AnimatedProgressBar progressBar;
	private View closeButton;
	private CloseListener closeListener;
	private FrameLayout progressCardsLayout;
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
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(closeListener!=null)
					closeListener.onClose();
			}
		});
		donateButton = mView.findViewById(R.id.info_donate_button);

		progressCardsLayout = mView.findViewById(R.id.info_progress_cards_layout);

		return mView;
	}

	public void fillInfo(final ProjectEntry project){
		mProject = project;

		infoTitle.setText(project.title);
		infoDescription.setText(project.description);
		infoMoney.setText(Utils.getProgressString(project.donationsCollected, project.donationsGoal));

		progressBar.setMax(project.donationsGoal);
		progressBar.setProgress(project.donationsCollected);

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


				View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.payment_dialog_layout, (ViewGroup) getView(), false);
				// Set up the input
				final TextInputEditText input = viewInflated.findViewById(R.id.payment_dialog_amount_input);

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
								// TODO: 14.05.2020 amount input and pay
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

		// TODO: 04.05.2020 progress cards
		getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.info_progress_cards_layout, new ProgressCardsFragment()).commit();
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
