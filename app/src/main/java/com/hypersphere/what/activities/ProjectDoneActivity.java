package com.hypersphere.what.activities;


import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.R;

import java.util.Random;

public class ProjectDoneActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_done);

		ViewGroup mainLayout = findViewById(R.id.activity_done_main_layout);

		MaterialButton button = findViewById(R.id.ok_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		TextView textView = findViewById(R.id.done_text_view);

		String name = getIntent().getExtras().get("projectName").toString();
		String text = textView.getText().toString().replace("1234567890", name);
		textView.setText(text);

		//someone said it looks obsolete
		/*
		Handler handler = new Handler();
		new Timer().scheduleAtFixedRate(new TimerTask() {
			Pair<Integer, Integer> last = new Pair<>(0, 0);
			@Override
			public void run() {

				if(mainLayout.getWidth() > 0) {
					Pair<Integer, Integer> p = generateRandPoint(mainLayout.getWidth(), mainLayout.getHeight());
					while(distanceP2P(p, last) < 100)
						p = generateRandPoint(mainLayout.getWidth(), mainLayout.getHeight());

					int[] colors = new int[]{Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA, Color.BLUE};

					Pair<Integer, Integer> finalP = p;
					handler.post(() -> CommonConfetti.explosion(mainLayout, finalP.first, finalP.second, colors).oneShot());
				}
			}
		}, 0, 300);
		 */
	}
	
	Pair<Integer, Integer> generateRandPoint(int w, int h){

		Random random = new Random();int x = Math.abs(random.nextInt()) % w, y = Math.abs(random.nextInt()) % h;

		if(x > w * 0.8)
			x = (int) (w * 0.8);
		if(x < w * 0.2)
			x = (int) (w * 0.2);
		if(y > h * 0.8)
			y = (int) (h * 0.8);
		if(y < h * 0.2)
			y = (int) (h * 0.2);
		return new Pair<>(x, y);
	}

	double distanceP2P(Pair<Integer,Integer> a, Pair<Integer,Integer> b){
		return Math.sqrt(Math.pow(a.first - b.first, 2) +Math.pow(a.second - b.second, 2));
	}
}
