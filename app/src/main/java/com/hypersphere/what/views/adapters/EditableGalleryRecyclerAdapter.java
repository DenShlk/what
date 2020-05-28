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

package com.hypersphere.what.views.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.helpers.MediaHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents gallery with add/change image functional.
 */
public class EditableGalleryRecyclerAdapter extends RecyclerView.Adapter<EditableGalleryRecyclerAdapter.ImageViewHolder> {

	private final List<Bitmap> images;
	private final OnResultCallbackActivity activity;

	private final int minImagesCount = 10;

	public EditableGalleryRecyclerAdapter(OnResultCallbackActivity activity) {
		this.activity = activity;

		images = new ArrayList<>();
		for (int i = 0; i < minImagesCount; i++) {
			images.add(null);
		}

		notifyItemRangeInserted(0, minImagesCount);
	}

	@NonNull
	@Override
	public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_add_image_item, parent, false);
		return new ImageViewHolder(layoutView);
	}

	/**
	 * Add image as last element of list.
	 *
	 * @param image
	 */
	public void addImage(Bitmap image){
		for (int i = 0; i < images.size() - 1; i++) {
			if(images.get(i)==null){
				images.set(i, image);
				notifyItemChanged(i);
				return;
			}
		}

		images.set(images.size() - 1, image);
		notifyItemChanged(images.size() - 1);
		images.add(null);
		notifyItemRangeInserted(images.size() - 1, 1);
	}

	/**
	 * Returns bitmaps that contains in gallery now.
	 * @return
	 */
	public List<Bitmap> getImages(){
		List<Bitmap> res = new ArrayList<>();
		for(Bitmap bmp : images)
			if(bmp!=null)
				res.add(bmp);
		return res;
	}

	/**
	 * Replaces image at given position.
	 * @param image
	 * @param position
	 */
	public void replaceImage(Bitmap image, int position){
		images.set(position, image);
		notifyItemChanged(position);
	}

	/**
	 * Removes image at current position.
	 * @param position
	 */
	public void deleteImage(int position){
		images.remove(position);
		notifyItemRemoved(position);

		if(images.size() < minImagesCount)
			images.add(null);
	}

	@Override
	public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
		holder.setImage(images.get(position));
	}

	@Override
	public int getItemCount() {
		return images.size();
	}

	/**
	 * Holder of image item. Opens bottom dialog with add or replace image functional.
	 */
	public class ImageViewHolder extends RecyclerView.ViewHolder implements OnResultCallbackActivity.ActivityResultListener {
		private static final int REQUEST_NEW_IMAGE_CAPTURE = 342;
		private static final int REQUEST_NEW_IMAGE_FROM_GALLERY = 3543;
		private static final int REQUEST_IMAGE_RECAPTURE = 5451;
		private static final int REQUEST_REOPEN_IMAGE_FROM_GALLERY = 3143;
		private Bitmap image;
		private final ImageView imageView;
		private final ImageView addButton;

		ImageViewHolder(@NonNull View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image_view);
			addButton = itemView.findViewById(R.id.add_button);

			imageView.setImageBitmap(null);

			addButton.setOnClickListener(v -> {
				final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
				mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
				View sheetView = activity.getLayoutInflater().inflate(R.layout.add_image_dialog_layout, null);
				mBottomSheetDialog.setContentView(sheetView);
				mBottomSheetDialog.show();

				View takeButton = sheetView.findViewById(R.id.dialog_take_photo);
				takeButton.setOnClickListener(v17 -> {
					Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					activity.startActivityForResult(cameraIntent, REQUEST_NEW_IMAGE_CAPTURE);
					activity.addActivityResultListener(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

				View galleryButton = sheetView.findViewById(R.id.dialog_open_gallery);
				galleryButton.setOnClickListener(v16 -> {
					Intent galleryIntent = new Intent(Intent.ACTION_PICK);
					galleryIntent.setType("image/*");
					galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
					activity.startActivityForResult(galleryIntent, REQUEST_NEW_IMAGE_FROM_GALLERY);
					activity.addActivityResultListener(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

			});

			imageView.setOnClickListener(v -> {
				final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
				mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
				View sheetView = activity.getLayoutInflater().inflate(R.layout.redact_image_dialog_layout, null);
				mBottomSheetDialog.setContentView(sheetView);
				mBottomSheetDialog.show();

				View viewButton = sheetView.findViewById(R.id.dialog_view_image);
				viewButton.setOnClickListener(v15 -> {
					final Dialog showDialog = new Dialog(activity, android.R.style.Theme_Translucent);
					showDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					showDialog.setCancelable(true);
					showDialog.setContentView(R.layout.image_viewer_layout);
					ImageView imageView = showDialog.findViewById(R.id.image_view);
					imageView.setImageBitmap(image);
					View backButton = showDialog.findViewById(R.id.back_button);
					backButton.setOnClickListener(v14 -> showDialog.dismiss());
					showDialog.show();
					mBottomSheetDialog.cancel();
				});

				View retakeButton = sheetView.findViewById(R.id.dialog_retake_photo);
				retakeButton.setOnClickListener(v13 -> {

					Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					activity.startActivityForResult(cameraIntent, REQUEST_IMAGE_RECAPTURE);
					activity.addActivityResultListener(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

				View reopenButton = sheetView.findViewById(R.id.dialog_reopen_gallery);
				reopenButton.setOnClickListener(v12 -> {

					Intent galleryIntent = new Intent(Intent.ACTION_PICK);
					galleryIntent.setType("image/*");
					galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
					activity.startActivityForResult(galleryIntent, REQUEST_REOPEN_IMAGE_FROM_GALLERY);
					activity.addActivityResultListener(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

				View deleteButton = sheetView.findViewById(R.id.dialog_delete_photo);
				deleteButton.setOnClickListener(v1 -> {
					deleteImage(getAdapterPosition());

					mBottomSheetDialog.cancel();
				});
			});

		}

		/**
		 * Hides add image button and shows ImageView with given image.
		 * @param image if null returns Holder to initial state.
		 */
		public void setImage(Bitmap image) {
			this.image = image;
			if(image == null){
				addButton.setVisibility(View.VISIBLE);
				imageView.setVisibility(View.GONE);
			}else{
				addButton.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
			}
			imageView.setImageBitmap(image);
		}

		/**
		 * Listens result of bottom dialog.
		 *
		 * @param requestCode
		 * @param resultCode
		 * @param data
		 */
		public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
			if (requestCode == REQUEST_NEW_IMAGE_CAPTURE) {
				activity.removeActivityResultListener(ImageViewHolder.this);

				if (resultCode == Activity.RESULT_OK) {
					Bitmap photo = (Bitmap) data.getExtras().get("data");
					addImage(photo);
				}
			}
			if (requestCode == REQUEST_NEW_IMAGE_FROM_GALLERY) {
				activity.removeActivityResultListener(ImageViewHolder.this);

				if (resultCode == Activity.RESULT_OK) {
					Uri photoUri = data.getData();
					Bitmap photo = MediaHelper.getImageByUri(photoUri);
					if(photo != null)
						addImage(photo);
				}
			}

			if (requestCode == REQUEST_IMAGE_RECAPTURE) {
				activity.removeActivityResultListener(ImageViewHolder.this);

				if (resultCode == Activity.RESULT_OK) {
					Bitmap photo = (Bitmap) data.getExtras().get("data");
					replaceImage(photo, getAdapterPosition());
				}
			}
			if (requestCode == REQUEST_REOPEN_IMAGE_FROM_GALLERY) {
				activity.removeActivityResultListener(ImageViewHolder.this);

				if (resultCode == Activity.RESULT_OK) {
					Uri photoUri = data.getData();
					Bitmap photo = MediaHelper.getImageByUri(photoUri);
					if(photo != null)
						replaceImage(photo, getAdapterPosition());
				}
			}
		}
	}

}

