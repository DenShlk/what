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
import com.hypersphere.what.model.UserEntry;

public class SplashActivity extends AppCompatActivity {

	private static final int SPLASH_DURATION = 3000;

	private final Handler timer = new Handler();

	private Intent nextActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);

		//animations
		Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.from_top_animation);
		Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation);
		topAnim.setInterpolator(new DecelerateInterpolator());
		bottomAnim.setInterpolator(new DecelerateInterpolator());

		//Hooks
		TextView topText = findViewById(R.id.slogan_text_1);
		TextView bottomText = findViewById(R.id.slogan_text_2);
		View splashLayout = findViewById(R.id.splash_layout);

		topText.setAnimation(topAnim);
		bottomText.setAnimation(bottomAnim);


		if (CloudManager.isLoginNeed()) {
			nextActivity = new Intent(SplashActivity.this, LoginActivity.class);
			timer.postDelayed(() -> {
				startActivity(nextActivity);
				finish();
			}, SPLASH_DURATION);
			splashLayout.setOnClickListener(v -> {
				v.setEnabled(false);
				timer.removeCallbacksAndMessages(null);

				startActivity(nextActivity);
				finish();
			});
		}
		else {
			nextActivity = new Intent(SplashActivity.this, MainActivity.class);
			CloudManager.setUserDownloadListener(new CloudManager.OnDownloadListener<UserEntry>() {
				@Override
				public void onComplete(UserEntry data) {
					startActivity(nextActivity);
					finish();
				}

				@Override
				public void onCancel() {}
			});
		}

	}
}
