package com.hypersphere.what.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.R;
import com.hypersphere.what.views.TutorialPageAdapter;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

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
				if(position == adapter.getItemCount() - 1)
					nextButton.setText(getResources().getText(R.string.tutorial_done));
			}
		});

		//different tutorial pages has different colors. So, we don't won't to change status bar color, we just hide it.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void navigateToNext(){
		Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
