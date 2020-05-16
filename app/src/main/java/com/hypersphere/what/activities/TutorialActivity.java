package com.hypersphere.what.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.R;
import com.hypersphere.what.views.TutorialPageAdapter;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class TutorialActivity extends AppCompatActivity {

	private ViewPager2 viewPager;
	private WormDotsIndicator dotsIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);

		viewPager = findViewById(R.id.tutor_view_pager);
		dotsIndicator = findViewById(R.id.tutor_dots_indicator);

		TutorialPageAdapter adapter = new TutorialPageAdapter();
		viewPager.setAdapter(adapter);
		dotsIndicator.setViewPager2(viewPager);

		MaterialButton nextButton = findViewById(R.id.tutor_next_button);
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(viewPager.getCurrentItem() == adapter.getItemCount() - 1) {
					navigateToNext();
				}
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
				if(viewPager.getCurrentItem() == adapter.getItemCount() - 1){
					// TODO: 15.05.2020 to res
					nextButton.setText("Done");
				}
			}
		});
		MaterialButton skipButton = findViewById(R.id.tutor_skip_button);
		skipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				navigateToNext();
			}
		});


		//different pages has different colors. So, we don't won't to change status bar color, we just hide it.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void navigateToNext(){
		Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
