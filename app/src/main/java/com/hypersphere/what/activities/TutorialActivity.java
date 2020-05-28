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

package com.hypersphere.what.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.R;
import com.hypersphere.what.views.adapters.TutorialPageAdapter;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

/**
 * Shows to user short information about application.
 * Has ViewPager2 with {@link TutorialPageAdapter}.
 */
public class TutorialActivity extends AppCompatActivity {

	private ViewPager2 viewPager;
	private TutorialPageAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);

		viewPager = findViewById(R.id.tutor_view_pager);
		viewPager.setOffscreenPageLimit(3);
		WormDotsIndicator dotsIndicator = findViewById(R.id.tutor_dots_indicator);

		adapter = new TutorialPageAdapter();
		viewPager.setAdapter(adapter);
		dotsIndicator.setViewPager2(viewPager);

		MaterialButton nextButton = findViewById(R.id.tutor_next_button);
		nextButton.setOnClickListener(v -> {
			if(viewPager.getCurrentItem() == adapter.getItemCount() - 1) {
				//last page
				navigateToNext();
			}
			viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
		});
		MaterialButton skipButton = findViewById(R.id.tutor_skip_button);
		skipButton.setOnClickListener(v -> navigateToNext());

		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				//if current page is last changes nextButton text
				if (position == adapter.getItemCount() - 1)
					nextButton.setText(getResources().getText(R.string.tutorial_done));
			}
		});

		//different tutorial pages has different colors. So, we don't want to change status bar color, we just hide it.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * Starts {@link MainActivity}.
	 */
	private void navigateToNext(){
		Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
