package com.hypersphere.what.activities;

import android.animation.TimeInterpolator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;

public class LoginActivity extends AppCompatActivity {

	private static final int LOGIN_MODE = 1;
	private static final int SIGN_MODE = 2;

	private int mode = SIGN_MODE;

	private Animation toLeft;
	private Animation toRight;
	private Animation fromLeft;
	private Animation fromRight;

	private TextInputLayout loginUserId;
	private TextInputLayout loginPassword;
	private TextInputLayout signUsername;
	private TextInputLayout signPassword;
	private TextInputLayout signEmail;
	private LinearLayout loginButtons, signButtons, mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		loginUserId = findViewById(R.id.login_user_id_layout);
		loginPassword = findViewById(R.id.login_password_layout);
		signUsername = findViewById(R.id.sign_username_layout);
		signPassword = findViewById(R.id.sign_password_layout);
		signEmail = findViewById(R.id.sign_email_layout);

		loginButtons = findViewById(R.id.login_buttons_layout);
		signButtons = findViewById(R.id.sign_buttons_layout);
		mainLayout = findViewById(R.id.inputs_layout);

		toLeft = AnimationUtils.loadAnimation(this, R.anim.to_left_animation);
		toRight = AnimationUtils.loadAnimation(this, R.anim.to_right_animation);
		fromLeft = AnimationUtils.loadAnimation(this, R.anim.from_left_animation);
		fromRight = AnimationUtils.loadAnimation(this, R.anim.from_right_animation);
		toLeft.setInterpolator(new AccelerateInterpolator());
		toRight.setInterpolator(new AccelerateInterpolator());
		fromLeft.setInterpolator(new DecelerateInterpolator());
		fromRight.setInterpolator(new DecelerateInterpolator());

		loginUserId.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			String email = v.getText().toString();
			if (!checkEmail(email))
				loginUserId.setError("Email invalid");
			else
				loginUserId.setError("");
			return false;
		});
		loginPassword.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			String password = v.getText().toString();
			if (checkPassword(password))
				loginUserId.setError("At least 6 characters");
			else
				loginUserId.setError("");
			return false;
		});
	}

	private boolean checkEmail(String email) {
		return email.matches(".+@.+\\..+");
	}

	private boolean checkPassword(String password) {
		return password.length() >= 6;
	}

	public void loginClick(View v) {
		String email = loginUserId.getEditText().getText().toString();
		String password = loginPassword.getEditText().getText().toString();

		if (checkEmail(email) && checkPassword(password))
			CloudManager.login(email, password, new CloudManager.OnAuthListener() {
				@Override
				public void onSuccess() {
					navigateToNext(false);
				}

				@Override
				public void onError() {
					errorAnimate();
				}
			});

		else {
			errorAnimate();
		}
	}

	public void signClick(View v) {
		String username = signUsername.getEditText().getText().toString();
		String email = signEmail.getEditText().getText().toString();
		String password = signPassword.getEditText().getText().toString();


		if (checkEmail(email) && checkPassword(password)) {
			CloudManager.sign(username, email, password, new CloudManager.OnAuthListener() {
				@Override
				public void onSuccess() {
					navigateToNext(true);
				}

				@Override
				public void onError() {
					errorAnimate();
				}
			});

		} else
			errorAnimate();
	}

	private void navigateToNext(boolean isNewUser) {
		Intent intent;
		if(isNewUser)
			intent = new Intent(LoginActivity.this, TutorialActivity.class);
		else
			intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void errorAnimate() {
		final float FREQ = 1.6f;
		final float DECAY = 1.1f;
		// interpolator that goes 1 -> -1 -> 1 -> -1 in a sine wave pattern.
		TimeInterpolator decayingSineWave = input -> {
			double raw = Math.sin(FREQ * input * 2 * Math.PI);
			return (float) (raw * Math.exp(-input * DECAY));
		};

		mainLayout.clearAnimation();
		mainLayout.setTranslationX(0);
		mainLayout.animate()
				.translationXBy(-50)
				.setInterpolator(decayingSineWave)
				.setDuration(500)
				.start();
	}

	public void newUserClick(View view) {
		mode = SIGN_MODE;
		loginUserId.startAnimation(toLeft);
		loginPassword.startAnimation(toRight);
		loginButtons.startAnimation(toLeft);

		new Handler().postDelayed(() -> {
			loginUserId.setVisibility(View.GONE);
			loginPassword.setVisibility(View.GONE);
			loginButtons.setVisibility(View.GONE);

			signUsername.setVisibility(View.VISIBLE);
			signEmail.setVisibility(View.VISIBLE);
			signPassword.setVisibility(View.VISIBLE);
			signButtons.setVisibility(View.VISIBLE);
			signUsername.startAnimation(fromRight);
			signEmail.startAnimation(fromLeft);
			signPassword.startAnimation(fromRight);
			signButtons.startAnimation(fromLeft);

		}, 250);
	}

	public void haveAccountClick(View view) {
		mode = LOGIN_MODE;
		signUsername.startAnimation(toRight);
		signEmail.startAnimation(toLeft);
		signPassword.startAnimation(toRight);
		signButtons.startAnimation(toLeft);

		new Handler().postDelayed(() -> {
			signUsername.setVisibility(View.GONE);
			signEmail.setVisibility(View.GONE);
			signPassword.setVisibility(View.GONE);
			signButtons.setVisibility(View.GONE);

			loginUserId.setVisibility(View.VISIBLE);
			loginPassword.setVisibility(View.VISIBLE);
			loginButtons.setVisibility(View.VISIBLE);
			loginUserId.startAnimation(fromLeft);
			loginPassword.startAnimation(fromRight);
			loginButtons.startAnimation(fromLeft);

		}, 250);
	}
}
