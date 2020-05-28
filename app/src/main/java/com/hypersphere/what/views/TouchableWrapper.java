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

package com.hypersphere.what.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Used in {@link com.hypersphere.what.activities.LocationSelectActivity} to notify context about
 * touch events.
 */
public class TouchableWrapper extends FrameLayout {
	public TouchableWrapper(@NonNull Context context) {
		super(context);
	}

	public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	private onCameraMoveEndListener listener;
	private int state = 0;
	private static final int STATE_BEFORE = 0;
	private static final int STATE_DOWN = 1;
	private static final int STATE_END = 2;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if(state == STATE_BEFORE && ev.getAction() == MotionEvent.ACTION_DOWN)
			state++; //user touches screen
		else{
			if(state==STATE_DOWN && ev.getAction() == MotionEvent.ACTION_MOVE)
				state++; //user moves finger
			else{
				if (state == STATE_END && ev.getAction() == MotionEvent.ACTION_UP) {
					//user stops movement
					listener.onCameraMoveEnd();
					state = STATE_BEFORE;
				} else {
					if (state != STATE_END || ev.getAction() != MotionEvent.ACTION_MOVE) {
						//during movement
						state = STATE_BEFORE;
					}
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	public void setListener(onCameraMoveEndListener listener) {
		this.listener = listener;
	}

	/**
	 * Interface to notify about finish of move.
	 */
	public interface onCameraMoveEndListener{
		void onCameraMoveEnd();
	}
}
