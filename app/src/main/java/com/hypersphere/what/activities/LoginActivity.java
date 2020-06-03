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
import com.hypersphere.what.R;
import com.hypersphere.what.helpers.CloudHelper;

/**
 * Provides an interface for the user to log in or register
 * When user change mode (log in / register) starts animations.
 */
public class LoginActivity extends AppCompatActivity {

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
			if (!checkEmail(email)) {
				loginUserId.setError(getString(R.string.login_error_email_invalid));
			} else loginUserId.setError(null);
			return false;
		});
		loginPassword.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			String password = v.getText().toString();
			if (!checkPassword(password)) {
				loginPassword.setError(getString(R.string.login_error_password_invalid));
			} else loginPassword.setError(null);
			return false;
		});

		signUsername.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			String username = v.getText().toString();
			if (!checkUsername(username)) {
				signUsername.setError(getString(R.string.login_error_username_empty));
			} else signUsername.setError(null);
			return false;
		});
		signEmail.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			String email = v.getText().toString();
			if (!checkEmail(email)) {
				signEmail.setError(getString(R.string.login_error_email_invalid));
			} else signEmail.setError(null);
			return false;
		});
		signPassword.getEditText().setOnEditorActionListener((v, actionId, event) -> {
			String password = v.getText().toString();
			if (!checkPassword(password)) {
				signPassword.setError(getString(R.string.login_error_password_invalid));
			} else signPassword.setError(null);
			return false;
		});
	}

	/**
	 * Checks username correctness.
	 *
	 * @param username
	 * @return
	 */
	private boolean checkUsername(String username) {
		return !username.isEmpty();
	}

	/**
	 * Checks email correctness.
	 *
	 * @param email
	 * @return true if email is correct.
	 */
	private boolean checkEmail(String email) {
		return email.matches(".+@.+\\..+");
	}

	/**
	 * Checks password correctness (syntactically like not empty, it isn't connect to server).
	 * @param password
	 * @return true if password is correct.
	 */
	private boolean checkPassword(String password) {
		return password.length() >= 6;
	}

	/**
	 * Tries to login with entered email and password. Firstly checks their correctness.
	 * @param v
	 */
	public void loginClick(View v) {
		String email = loginUserId.getEditText().getText().toString();
		String password = loginPassword.getEditText().getText().toString();

		if (checkEmail(email) && checkPassword(password))
			CloudHelper.login(email, password, new CloudHelper.OnAuthListener() {
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

	/**
	 * Tries to register with entered username, email and password. Firstly checks their correctness.
	 * @param v
	 */
	public void signClick(View v) {
		String username = signUsername.getEditText().getText().toString();
		String email = signEmail.getEditText().getText().toString();
		String password = signPassword.getEditText().getText().toString();
		if (checkUsername(username) && checkEmail(email) && checkPassword(password)) {
			CloudHelper.sign(username, email, password, new CloudHelper.OnAuthListener() {
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

	/**
	 * Starts next activity. If user was registered it {@link TutorialActivity} and
	 * {@link MainActivity} otherwise.
	 * @param isNewUser true if user was registered or false if user was logged
	 */
	private void navigateToNext(boolean isNewUser) {
		Intent intent;
		if (isNewUser)
			intent = new Intent(LoginActivity.this, TutorialActivity.class);
		else
			intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Starts shake animation on input fields.
	 */
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

	/**
	 * Starts transition to register mode.
	 * @param view
	 */
	public void newUserClick(View view) {
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

	/**
	 * Starts transition to log in mode.
	 * @param view
	 */
	public void haveAccountClick(View view) {
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
