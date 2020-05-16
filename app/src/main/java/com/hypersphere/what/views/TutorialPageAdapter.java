package com.hypersphere.what.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;

public class TutorialPageAdapter extends RecyclerView.Adapter<TutorialPageAdapter.TutorialItemHolder> {

	int[] items = {R.layout.tutorial_item_1_layout, R.layout.tutorial_item_2_layout, R.layout.tutorial_item_3_layout };

	@NonNull
	@Override
	public TutorialItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(items[viewType], parent, false);
		return new TutorialItemHolder(layoutView);
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public void onBindViewHolder(@NonNull TutorialItemHolder holder, int position) {
	}

	public TutorialPageAdapter() {
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return items.length;
	}

	class TutorialItemHolder extends RecyclerView.ViewHolder {

		public TutorialItemHolder(@NonNull View itemView) {
			super(itemView);
		}
	}
}
