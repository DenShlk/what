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

package com.hypersphere.what;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;

/**
 * Class for listen onActivityResult from Activity extends it from fragments and etc.
 */
public abstract class OnResultCallbackActivity extends AppCompatActivity {

	//set of listeners (to avoid double call in one class)
	private final HashSet<ActivityResultListener> activityResultListeners = new HashSet<>();

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		synchronized (activityResultListeners) {
			for (ActivityResultListener listener : activityResultListeners)
				listener.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void addActivityResultListener(ActivityResultListener listener) {
		activityResultListeners.add(listener);
	}

	public void removeActivityResultListener(ActivityResultListener listener) {
		activityResultListeners.remove(listener);
	}

	/**
	 * Interface for listen onActivityResult calls
	 */
	public interface ActivityResultListener {
		void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
	}
}
