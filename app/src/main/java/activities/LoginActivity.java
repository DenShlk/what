package activities;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hypersphere.what.R;

public class LoginActivity extends AppCompatActivity {

	private static final int LOGIN_MODE = 1;
	private static final int SIGN_MODE = 2;

	int mode = SIGN_MODE;

	Animation toLeft, toRight, fromLeft, fromRight;

	TextInputLayout loginUserId, loginPassword, signUsername, signPassword, signEmail, signPhone;
	LinearLayout loginButtons, signButtons;

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

		toLeft = AnimationUtils.loadAnimation(this, R.anim.to_left_animation);
		toRight = AnimationUtils.loadAnimation(this, R.anim.to_right_animation);
		fromLeft = AnimationUtils.loadAnimation(this, R.anim.from_left_animation);
		fromRight = AnimationUtils.loadAnimation(this, R.anim.from_right_animation);
		toLeft.setInterpolator(new AccelerateInterpolator());
		toRight.setInterpolator(new AccelerateInterpolator());
		fromLeft.setInterpolator(new DecelerateInterpolator());
		fromRight.setInterpolator(new DecelerateInterpolator());

		final MaterialButton loginButton = findViewById(R.id.login_login_button);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginButton.setClickable(false);
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			}
		});
		final MaterialButton signButton = findViewById(R.id.login_sign_button);
		signButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signButton.setClickable(false);
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			}
		});
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
				signPhone.setVisibility(View.VISIBLE);
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
