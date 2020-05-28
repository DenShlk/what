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

package com.hypersphere.what.helpers;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

/**
 * Class that can control (hide or open keyboard).
 * Extends {@link KeyboardVisibilityEvent} to collect all used in project keyboard cases.
 */
public class KeyboardHelper extends KeyboardVisibilityEvent {

	/**
	 * Hides keyboard
	 *
	 * @param view used to get SystemService and WindowToken
	 */
	public static void hideKeyboard(@NonNull View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * Opens keyboard focused on given view
	 *
	 * @param view view to focus on
	 */
	public static void openKeyboard(@NonNull View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}
}
