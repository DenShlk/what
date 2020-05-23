package com.hypersphere.what.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	public List<Bitmap> getImages(){
		List<Bitmap> res = new ArrayList<>();
		for(Bitmap bmp : images)
			if(bmp!=null)
				res.add(bmp);
		return res;
	}

	public void replaceImage(Bitmap image, int position){
		images.set(position, image);
		notifyItemChanged(position);
	}

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

	public class ImageViewHolder extends RecyclerView.ViewHolder{
		private static final int REQUEST_NEW_IMAGE_CAPTURE = 1;
		private static final int REQUEST_NEW_IMAGE_FROM_GALLERY = 2;
		private static final int REQUEST_IMAGE_RECAPTURE = 3;
		private static final int REQUEST_REOPEN_IMAGE_FROM_GALLERY = 4;


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
				View sheetView = activity.getLayoutInflater().inflate(R.layout.add_image_dialog_layout, null);
				mBottomSheetDialog.setContentView(sheetView);
				mBottomSheetDialog.show();

				View takeButton = sheetView.findViewById(R.id.dialog_take_photo);
				takeButton.setOnClickListener(v17 -> {
					Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					activity.startActivityForResult(cameraIntent, REQUEST_NEW_IMAGE_CAPTURE);
					activity.setWaitForCallback(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

				View galleryButton = sheetView.findViewById(R.id.dialog_open_gallery);
				galleryButton.setOnClickListener(v16 -> {
					Intent galleryIntent = new Intent(Intent.ACTION_PICK);
					galleryIntent.setType("image/*");
					galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
					activity.startActivityForResult(galleryIntent, REQUEST_NEW_IMAGE_FROM_GALLERY);
					activity.setWaitForCallback(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

			});

			imageView.setOnClickListener(v -> {
				final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
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
					activity.setWaitForCallback(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

				View reopenButton = sheetView.findViewById(R.id.dialog_reopen_gallery);
				reopenButton.setOnClickListener(v12 -> {

					Intent galleryIntent = new Intent(Intent.ACTION_PICK);
					galleryIntent.setType("image/*");
					galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
					activity.startActivityForResult(galleryIntent, REQUEST_REOPEN_IMAGE_FROM_GALLERY);
					activity.setWaitForCallback(ImageViewHolder.this);

					mBottomSheetDialog.cancel();
				});

				View deleteButton = sheetView.findViewById(R.id.dialog_delete_photo);
				deleteButton.setOnClickListener(v1 -> {
					deleteImage(getAdapterPosition());

					mBottomSheetDialog.cancel();
				});
			});

		}

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
		
		public void onResult(int requestCode, int resultCode, @Nullable Intent data){
			if(requestCode == REQUEST_NEW_IMAGE_CAPTURE) {
				activity.setWaitForCallback(null);

				if (resultCode == Activity.RESULT_OK) {
					Bitmap photo = (Bitmap) data.getExtras().get("data");
					addImage(photo);
				}
			}
			if(requestCode == REQUEST_NEW_IMAGE_FROM_GALLERY) {
				activity.setWaitForCallback(null);

				if (resultCode == Activity.RESULT_OK) {
					Uri photoUri = data.getData();
					Bitmap photo = getImageByUri(photoUri);
					if(photo != null)
						addImage(photo);
				}
			}

			if(requestCode == REQUEST_IMAGE_RECAPTURE) {
				activity.setWaitForCallback(null);

				if (resultCode == Activity.RESULT_OK) {
					Bitmap photo = (Bitmap) data.getExtras().get("data");
					replaceImage(photo, getAdapterPosition());
				}
			}
			if(requestCode == REQUEST_REOPEN_IMAGE_FROM_GALLERY) {
				activity.setWaitForCallback(null);

				if (resultCode == Activity.RESULT_OK) {
					Uri photoUri = data.getData();
					Bitmap photo = getImageByUri(photoUri);
					if(photo != null)
						replaceImage(photo, getAdapterPosition());
				}
			}
		}

		private Bitmap getImageByUri(Uri uri){
			try {
				ParcelFileDescriptor parcelFileDescriptor =
						activity.getContentResolver().openFileDescriptor(uri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
				Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

				parcelFileDescriptor.close();

				return image;
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}
	
}

