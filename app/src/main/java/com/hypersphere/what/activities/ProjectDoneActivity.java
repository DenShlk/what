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

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.hypersphere.what.R;

/**
 * Activity for congratulate user with project finishing.
 */
public class ProjectDoneActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_done);

		MaterialButton button = findViewById(R.id.ok_button);
		button.setOnClickListener(v -> finish());

		TextView textView = findViewById(R.id.done_text_view);

		//put project name to congratulation text
		String name = getIntent().getExtras().get("projectName").toString();
		String text = textView.getText().toString().replace("1234567890", name);
		textView.setText(text);
	}
}
