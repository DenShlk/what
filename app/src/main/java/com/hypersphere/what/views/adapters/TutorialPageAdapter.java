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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;

/**
 * Presents tutorial pages.
 */
public class TutorialPageAdapter extends RecyclerView.Adapter<TutorialPageAdapter.TutorialItemHolder> {

	private final int[] items = {R.layout.tutorial_item_1_layout, R.layout.tutorial_item_2_layout, R.layout.tutorial_item_3_layout };

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

		TutorialItemHolder(@NonNull View itemView) {
			super(itemView);
		}
	}
}
