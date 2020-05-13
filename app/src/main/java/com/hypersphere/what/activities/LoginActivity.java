package com.hypersphere.what.activities;

import android.animation.TimeInterpolator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.R;

public class LoginActivity extends AppCompatActivity {

	private static final int LOGIN_MODE = 1;
	private static final int SIGN_MODE = 2;

	int mode = SIGN_MODE;

	Animation toLeft, toRight, fromLeft, fromRight;

	TextInputLayout loginUserId, loginPassword, signUsername, signPassword, signEmail, signPhone;
	LinearLayout loginButtons, signButtons, mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		loginUserId = findViewById(R.id.login_user_id_layout);
		loginPassword = findViewById(R.id.login_password_layout);
		signUsername = findViewById(R.id.sign_username_layout);
		signPassword = findViewById(R.id.sign_password_layout);
		signEmail = findViewById(R.id.sign_email_layout);
		signPhone = findViewById(R.id.sign_phone_layout);

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

		loginUserId.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				String email = v.getText().toString();
				if (!checkEmail(email))
					loginUserId.setError("Email invalid");
				else
					loginUserId.setError("");
				return false;
			}
		});
		loginPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				String password = v.getText().toString();
				if (checkPassword(password))
					loginUserId.setError("At least 6 characters");
				else
					loginUserId.setError("");
				return false;
			}
		});
	}

	boolean checkEmail(String email) {
		return email.matches(".+@.+\\..+");
	}

	boolean checkPassword(String password) {
		return password.length() >= 6;
	}

	public void loginClick(View v) {
		String email = loginUserId.getEditText().getText().toString();
		String password = loginPassword.getEditText().getText().toString();

		if (checkEmail(email) && checkPassword(password))
			CloudManager.login(email, password, new CloudManager.OnAuthListener() {
				@Override
				public void onSuccess() {
					navigateToMain();
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

	public void signupClick(View v) {
		String username = signUsername.getEditText().getText().toString();
		String email = signEmail.getEditText().getText().toString();
		String password = signPassword.getEditText().getText().toString();


		if (checkEmail(email) && checkPassword(password)) {
			CloudManager.sign(username, email, password, new CloudManager.OnAuthListener() {
				@Override
				public void onSuccess() {
					navigateToMain();
				}

				@Override
				public void onError() {
					errorAnimate();
				}
			});

		} else
			errorAnimate();
	}

	private void navigateToMain() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void errorAnimate() {
		final float FREQ = 1.6f;
		final float DECAY = 1.1f;
		// interpolator that goes 1 -> -1 -> 1 -> -1 in a sine wave pattern.
		TimeInterpolator decayingSineWave = new TimeInterpolator() {
			@Override
			public float getInterpolation(float input) {
				double raw = Math.sin(FREQ * input * 2 * Math.PI);
				return (float) (raw * Math.exp(-input * DECAY));
			}
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

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				loginUserId.setVisibility(View.GONE);
				loginPassword.setVisibility(View.GONE);
				loginButtons.setVisibility(View.GONE);

				signUsername.setVisibility(View.VISIBLE);
				signEmail.setVisibility(View.VISIBLE);
				//signPhone.setVisibility(View.VISIBLE);
				signPassword.setVisibility(View.VISIBLE);
				signButtons.setVisibility(View.VISIBLE);
				signUsername.startAnimation(fromRight);
				signEmail.startAnimation(fromLeft);
				signPhone.startAnimation(fromRight);
				signPassword.startAnimation(fromLeft);
				signButtons.startAnimation(fromRight);

			}
		}, 250);
	}

	public void haveAccountClick(View view) {
		mode = LOGIN_MODE;
		signUsername.startAnimation(toRight);
		signEmail.startAnimation(toLeft);
		signPhone.startAnimation(toRight);
		signPassword.startAnimation(toLeft);
		signButtons.startAnimation(toRight);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				signUsername.setVisibility(View.GONE);
				signEmail.setVisibility(View.GONE);
				signPhone.setVisibility(View.GONE);
				signPassword.setVisibility(View.GONE);
				signButtons.setVisibility(View.GONE);

				loginUserId.setVisibility(View.VISIBLE);
				loginPassword.setVisibility(View.VISIBLE);
				loginButtons.setVisibility(View.VISIBLE);
				loginUserId.startAnimation(fromLeft);
				loginPassword.startAnimation(fromRight);
				loginButtons.startAnimation(fromLeft);

			}
		}, 250);
	}
}