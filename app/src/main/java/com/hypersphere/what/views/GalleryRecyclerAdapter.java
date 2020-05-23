package com.hypersphere.what.views;

import android.app.Activity;
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

import java.util.ArrayList;
import java.util.List;

public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.ImageViewHolder> {

	private final List<Bitmap> images = new ArrayList<>(0);
	private final Activity activity;

	public GalleryRecyclerAdapter(Activity activity) {
		this.activity = activity;
	}

	@NonNull
	@Override
	public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_image_item, parent, false);
		return new ImageViewHolder(layoutView);
	}

	public void addImages(List<Bitmap> list){
		for(Bitmap bmp : list)
			addImage(bmp);
	}

	public void addImage(@NonNull Bitmap image){
		images.add(image);
		notifyItemRangeInserted(images.size() - 1, 1);
	}

	public void addImage(@NonNull Bitmap image, int index){
		images.add(index, image);
		notifyItemRangeInserted(index, 1);
	}

	public void clear(){
		int sizeWas = images.size();
		images.clear();
		notifyItemRangeRemoved(0, sizeWas);
	}

	public void replaceImage(Bitmap image, int position){
		images.set(position, image);
		notifyItemChanged(position);
	}

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

	public class ImageViewHolder extends RecyclerView.ViewHolder{

		private Bitmap image;
		private final ImageView imageView;

		ImageViewHolder(@NonNull View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image_view);

			imageView.setImageBitmap(null);


			imageView.setOnClickListener(v -> {
				final Dialog showDialog = new Dialog(activity, android.R.style.Theme_Translucent);
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

