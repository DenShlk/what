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

import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;
import com.hypersphere.what.WhatApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents list of images.
 */
public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.ImageViewHolder> {

	private final List<Bitmap> images = new ArrayList<>(0);

	public GalleryRecyclerAdapter() {
	}

	@NonNull
	@Override
	public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_image_item, parent, false);
		return new ImageViewHolder(layoutView);
	}

	/**
	 * Add to recycler list of images.
	 *
	 * @param list
	 */
	public void addImages(List<Bitmap> list){
		for(Bitmap bmp : list)
			addImage(bmp);
	}

	/**
	 * Add to recycler single image.
	 * @param image
	 */
	public void addImage(@NonNull Bitmap image){
		images.add(image);
		notifyItemRangeInserted(images.size() - 1, 1);
	}

	/**
	 * Add to recycler single image to given index.
	 * @param image
	 * @param index
	 */
	public void addImage(@NonNull Bitmap image, int index){
		images.add(index, image);
		notifyItemRangeInserted(index, 1);
	}

	/**
	 * Deletes all images from recycler.
	 */
	public void clear(){
		int sizeWas = images.size();
		images.clear();
		notifyItemRangeRemoved(0, sizeWas);
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
	 * Removes image at given position.
	 * @param position
	 */
	public void deleteImage(int position){
		images.remove(position);
		notifyItemRemoved(position);
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
	 * Holder of image. Shows bottom dialog on image click.
	 */
	public class ImageViewHolder extends RecyclerView.ViewHolder{

		private Bitmap image;
		private final ImageView imageView;

		ImageViewHolder(@NonNull View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image_view);

			imageView.setImageBitmap(null);
			imageView.setOnClickListener(v -> {
				final Dialog showDialog = new Dialog(WhatApplication.getContext(), android.R.style.Theme_Translucent);
				showDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				showDialog.setCancelable(true);
				showDialog.setContentView(R.layout.image_viewer_layout);
				ImageView imageView = showDialog.findViewById(R.id.image_view);
				imageView.setImageBitmap(image);
				View backButton = showDialog.findViewById(R.id.back_button);
				backButton.setOnClickListener(v1 -> showDialog.dismiss());
				showDialog.show();
			});

		}

		public void setImage(@NonNull Bitmap image) {
			this.image = image;
			imageView.setImageBitmap(image);
		}
	}
}

