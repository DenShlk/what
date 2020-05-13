package com.hypersphere.what.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.hypersphere.what.OnResultCallbackActivity;
import com.hypersphere.what.R;
import com.hypersphere.what.fragments.CreateProjectFragment;
import com.hypersphere.what.fragments.FeedFragment;
import com.hypersphere.what.fragments.GoogleMapFragment;
import com.hypersphere.what.fragments.ProfileFragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends OnResultCallbackActivity implements NavigationHost {

	FrameLayout frameLayout;

	Map<View, Fragment> menuMap = new HashMap<>();
	Map<View, Integer> menuOrder = new HashMap<>();
	List<Button> menuButtons;
	MenuController menuController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.app_bar);
		frameLayout = findViewById(R.id.main_fragment_container);

		Button menuMapButton = findViewById(R.id.menu_map_button);
		Button menuCreateButton = findViewById(R.id.menu_create_button);
		Button menuProfileButton = findViewById(R.id.menu_profile_button);
		Button menuFeedButton = findViewById(R.id.menu_feed_button);
		menuButtons = Arrays.asList(menuMapButton, menuFeedButton, menuCreateButton, menuProfileButton);

		menuMap.put(menuMapButton, new GoogleMapFragment());
		menuOrder.put(menuMapButton, 1);
		menuMap.put(menuFeedButton, new FeedFragment());
		menuOrder.put(menuFeedButton, 2);
		menuMap.put(menuCreateButton, new CreateProjectFragment());
		menuOrder.put(menuCreateButton, 3);
		menuMap.put(menuProfileButton, new ProfileFragment(null));
		menuOrder.put(menuProfileButton, 4);

		View.OnClickListener menuOnClick = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				navigateTo(menuMap.get(view), false);
				if(menuController.menuShown) {
					menuController.onClick();
				}

				for(Button b : menuButtons)
					b.setTextSize(15);

				Button button = (Button) view;
				button.setTextSize(18);
			}
		};

		menuMapButton.setOnClickListener(menuOnClick);
		menuFeedButton.setOnClickListener(menuOnClick);
		menuCreateButton.setOnClickListener(menuOnClick);
		menuProfileButton.setOnClickListener(menuOnClick);

		menuController = new MenuController(
				MainActivity.this,
				frameLayout,
				toolbar,
				new AccelerateDecelerateInterpolator(),
				getDrawable(R.drawable.ic_menu_black_24dp),
				getDrawable(R.drawable.ic_close_black_24dp));

		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(menuController);

		menuMapButton.callOnClick();
		//menuFeedButton.callOnClick();
		//menuCreateButton.callOnClick();
		//menuProfileButton.callOnClick();
	}

	@Override
	public void navigateTo(Fragment fragment, boolean addToBackstack) {
		FragmentTransaction transaction =
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.main_fragment_container, fragment);

		if (addToBackstack) {
			transaction.addToBackStack(null);
		}

		transaction.commit();
	}
}