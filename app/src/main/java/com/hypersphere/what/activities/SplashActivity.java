package com.hypersphere.what.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;

public class SplashActivity extends AppCompatActivity {

	private static final int SPLASH_DURATION = 3000;

	Animation topAnim, bottomAnim;

	TextView topText, bottomText;

	Handler timer = new Handler();

	Intent nextActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);

		if (CloudManager.isLoginNeed())
			nextActivity = new Intent(SplashActivity.this, LoginActivity.class);
		else
			nextActivity = new Intent(SplashActivity.this, MainActivity.class);

		//animations
		topAnim = AnimationUtils.loadAnimation(this, R.anim.from_top_animation);
		bottomAnim = AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation);
		topAnim.setInterpolator(new DecelerateInterpolator());
		bottomAnim.setInterpolator(new DecelerateInterpolator());

		//Hooks
		topText = findViewById(R.id.slogan_text_1);
		bottomText = findViewById(R.id.slogan_text_2);

		topText.setAnimation(topAnim);
		bottomText.setAnimation(bottomAnim);

		timer.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		}, SPLASH_DURATION);
	}

	public void onSplashClick(View view) {
		view.setEnabled(false);
		timer.removeCallbacksAndMessages(null);

		startActivity(nextActivity);
		finish();
	}
}
