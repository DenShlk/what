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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.fragments.CreateProjectFragment;
import com.hypersphere.what.fragments.FeedFragment;
import com.hypersphere.what.fragments.GoogleMapFragment;
import com.hypersphere.what.fragments.ProfileFragment;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.helpers.MenuHelper;
import com.hypersphere.what.model.ProjectEntry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main activity in app. Implements transition between fragments.
 */
public class MainActivity extends OnResultCallbackActivity {

	private final Map<View, Fragment> viewToFragmentMap = new HashMap<>();
	private final Map<MainFragmentsEnum, Fragment> nameToFragmentMap = new HashMap<>();
	private final Map<MainFragmentsEnum, Button> nameToButtonMap = new HashMap<>();
	private MenuHelper menuHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.app_bar);
		FrameLayout frameLayout = findViewById(R.id.main_fragment_container);

		Button menuMapButton = findViewById(R.id.menu_map_button);
		Button menuCreateButton = findViewById(R.id.menu_create_button);
		Button menuProfileButton = findViewById(R.id.menu_profile_button);
		Button menuFeedButton = findViewById(R.id.menu_feed_button);
		final List<Button> menuButtons = Arrays.asList(menuMapButton, menuFeedButton, menuCreateButton, menuProfileButton);

		Fragment mapFragment = new GoogleMapFragment();
		Fragment feedFragment = new FeedFragment();
		Fragment createFragment = new CreateProjectFragment();
		Fragment profileFragment = new ProfileFragment();

		//restore state
		if (false && savedInstanceState != null) {
			mapFragment = getSupportFragmentManager().getFragment(savedInstanceState, MainFragmentsEnum.Map.name());
			feedFragment = getSupportFragmentManager().getFragment(savedInstanceState, MainFragmentsEnum.Feed.name());
			createFragment = getSupportFragmentManager().getFragment(savedInstanceState, MainFragmentsEnum.CreateProject.name());
			profileFragment = getSupportFragmentManager().getFragment(savedInstanceState, MainFragmentsEnum.Profile.name());
		}

		viewToFragmentMap.put(menuMapButton, mapFragment);
		viewToFragmentMap.put(menuFeedButton, feedFragment);
		viewToFragmentMap.put(menuCreateButton, createFragment);
		viewToFragmentMap.put(menuProfileButton, profileFragment);

		nameToFragmentMap.put(MainFragmentsEnum.Map, mapFragment);
		nameToFragmentMap.put(MainFragmentsEnum.Feed, feedFragment);
		nameToFragmentMap.put(MainFragmentsEnum.CreateProject, createFragment);
		nameToFragmentMap.put(MainFragmentsEnum.Profile, profileFragment);

		nameToButtonMap.put(MainFragmentsEnum.Map, menuMapButton);
		nameToButtonMap.put(MainFragmentsEnum.Feed, menuFeedButton);
		nameToButtonMap.put(MainFragmentsEnum.CreateProject, menuCreateButton);
		nameToButtonMap.put(MainFragmentsEnum.Profile, menuProfileButton);

		View.OnClickListener menuOnClick = view -> {
			navigateTo(viewToFragmentMap.get(view));
			if (menuHelper.isMenuShown()) {
				menuHelper.onClick();
			}

			for (Button b : menuButtons) {
				b.setTextSize(15);
			}

			Button button = (Button) view;
			button.setTextSize(18);
		};

		menuMapButton.setOnClickListener(menuOnClick);
		menuFeedButton.setOnClickListener(menuOnClick);
		menuCreateButton.setOnClickListener(menuOnClick);
		menuProfileButton.setOnClickListener(menuOnClick);

		menuHelper = new MenuHelper(
				MainActivity.this,
				frameLayout,
				toolbar,
				new AccelerateDecelerateInterpolator(),
				getDrawable(R.drawable.ic_menu_black_24dp),
				getDrawable(R.drawable.ic_close_black_24dp));

		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(menuHelper);

		//set initial fragment
		if (savedInstanceState == null) {
			menuMapButton.callOnClick();
			//menuFeedButton.callOnClick();
			//menuCreateButton.callOnClick();
			//menuProfileButton.callOnClick();
		}

		CloudHelper.listenDoneProjects(projectId -> CloudHelper.loadProjectDone(projectId, new CloudHelper.OnDownloadListener<ProjectEntry>() {
			@Override
			public void onComplete(ProjectEntry data) {
				Intent intent = new Intent(MainActivity.this, ProjectDoneActivity.class);
				intent.putExtra("projectName", data.title);
				startActivity(intent);
			}

			@Override
			public void onCancel() {}
		}));
	}

	/**
	 * Saves fragments state.
	 */
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		for (MainFragmentsEnum fragment : MainFragmentsEnum.values()) {
			//getSupportFragmentManager().putFragment(outState, fragment.name(), nameToFragmentMap.get(fragment));
		}
	}

	/**
	 * Navigates to given fragment with animations as the user would do it.
	 *
	 * @param target
	 */
	public void smoothNavigateTo(MainFragmentsEnum target) {
		menuHelper.onClick();
		Handler handler = new Handler();
		handler.postDelayed(() -> nameToButtonMap.get(target).callOnClick(), MenuHelper.ANIMATION_DURATION);
	}

	/**
	 * Replaces fragment in R.id.main_fragment_container to given fragment
	 *
	 * @param fragment
	 */
	private void navigateTo(Fragment fragment) {
		FragmentTransaction transaction =
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.main_fragment_container, fragment);

		transaction.commit();
	}

	/**
	 * Fragments can be placed in main activity.
	 */
	public enum MainFragmentsEnum {
		Map,
		Feed,
		CreateProject,
		Profile,
	}
}