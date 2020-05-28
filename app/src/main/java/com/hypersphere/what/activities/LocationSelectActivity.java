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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.hypersphere.what.R;
import com.hypersphere.what.helpers.KeyboardHelper;
import com.hypersphere.what.views.TouchableWrapper;

import java.io.IOException;
import java.util.List;

/**
 * Calls in {@link com.hypersphere.what.fragments.CreateProjectFragment}
 * User can move map to place marker in center of screen to needed location.
 * Uses {@link TouchableWrapper} to detect the completion of map moves.
 */
public class LocationSelectActivity extends AppCompatActivity implements OnMapReadyCallback {

	private static final int LOCATION_REQUEST = 641;
	private static final String[] LOCATION_PERMS = {
			Manifest.permission.ACCESS_FINE_LOCATION
	};

	private GoogleMap googleMap;

	//Saint-Petersburg
	private LatLng defaultLocation = new LatLng(59.940805, 30.344595);

	private LocationManager locationManager;

	private EditText addressInput;
	private TouchableWrapper wrapper;
	private View searchButton;

	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_select);

		wrapper = findViewById(R.id.map_wrapper);

		Toolbar toolbar = findViewById(R.id.app_bar);
		toolbar.setNavigationOnClickListener(v -> {
			setResult(RESULT_CANCELED, new Intent());
			finish();
		});

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		preSetUpMap();

		setUpButtons();

		addressInput = findViewById(R.id.location_edit_text);

		findViewById(R.id.clear_button).setOnClickListener(v -> addressInput.setText(""));

		searchButton = findViewById(R.id.search_button);
		searchButton.setOnClickListener(v -> {
			if (addressInput.getText().length() > 0) {
				LatLng loc = getLocationFromAddress(String.valueOf(addressInput.getText()));
				if (loc != null) {
					googleMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
					addressInput.clearFocus();

					KeyboardHelper.hideKeyboard(addressInput);
				}
			}
		});

		addressInput.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE ||
					actionId == EditorInfo.IME_ACTION_GO ||
					actionId == EditorInfo.IME_ACTION_NEXT) {
				searchButton.callOnClick();
				return true;
			}
			return false;
		});

		//Send ok result
		findViewById(R.id.accept_button).setOnClickListener(v -> {
			Intent data = new Intent();
			data.putExtra("lat", googleMap.getCameraPosition().target.latitude);
			data.putExtra("lon", googleMap.getCameraPosition().target.longitude);
			data.putExtra("address", getAddressFromLocation(googleMap.getCameraPosition().target));
			setResult(Activity.RESULT_OK, data);
			finish();
		});
	}

	/**
	 * Sets onClicks to map-controls
	 * Runnable used to define loops action if button pressed
	 */
	private void setUpButtons(){
		findViewById(R.id.my_position_button).setOnClickListener(v -> googleMap.animateCamera(CameraUpdateFactory.newLatLng(getMyPosition())));

		final Handler handler = new Handler();

		View zoomInButton = findViewById(R.id.zoom_in_button);
		zoomInButton.setOnClickListener(v -> googleMap.animateCamera(CameraUpdateFactory.zoomIn()));
		final Runnable zoomInAction = new Runnable() {
			@Override
			public void run() {
				googleMap.animateCamera(CameraUpdateFactory.zoomBy(0.3f), 100, null);
				handler.postDelayed(this, 100);
			}
		};
		zoomInButton.setOnLongClickListener(v -> {
			handler.post(zoomInAction);
			return true;
		});
		zoomInButton.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				handler.removeCallbacks(zoomInAction);
				v.performClick();
			}
			return false;
		});

		final View zoomOutButton = findViewById(R.id.zoom_out_button);
		zoomOutButton.setOnClickListener(v -> googleMap.animateCamera(CameraUpdateFactory.zoomOut()));
		final Runnable zoomOutAction = new Runnable() {
			@Override
			public void run() {
				googleMap.animateCamera(CameraUpdateFactory.zoomBy(-0.3f), 100, null);
				handler.postDelayed(this, 100);
			}
		};
		zoomOutButton.setOnLongClickListener(v -> {
			handler.post(zoomOutAction);
			return true;
		});
		zoomOutButton.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				handler.removeCallbacks(zoomOutAction);
				v.performClick();
			}
			return false;
		});
	}

	/**
	 * Convert given String address to LatLng point
	 * @param strAddress String address
	 * @return LatLng point matches given address
	 */
	private LatLng getLocationFromAddress(String strAddress) {

		Geocoder geocoder = new Geocoder(this);
		List<Address> address;
		LatLng p1;

		try {
			address = geocoder.getFromLocationName(strAddress, 5);
			if (address == null) {
				return null;
			}
			Address location = address.get(0);
			location.getLatitude();
			location.getLongitude();

			p1 = new LatLng(location.getLatitude(),
					location.getLongitude());

			return p1;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Convert LatLng point to String address
	 * @param location LatLng point
	 * @return String address matches given point
	 */
	private String getAddressFromLocation(LatLng location) {

		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses;

		try {
			addresses = geocoder.getFromLocation(location.latitude, location.longitude, 5);
			if (addresses == null) {
				return null;
			}
			Address address = addresses.get(0);
			address.getLatitude();
			address.getLongitude();

			StringBuilder text = new StringBuilder(address.getAddressLine(0));
			for (int i = 1; i < address.getMaxAddressLineIndex(); i++) {
				text.append(", ").append(address.getAddressLine(i));
			}

			return text.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Starts map setup - load map (async) and request permission to get current location
	 */
	private void preSetUpMap() {
		SupportMapFragment googleMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
		googleMapFragment.getMapAsync(this);

		if (!hasGPSPermission()) {
			requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
		}

	}

	/**
	 * Calls setUpMap without checking results.
	 * If permission granted it will move camera to current location.
	 * Otherwise camera will be moved to default location.
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == LOCATION_REQUEST) {
			setUpMap();
		}
	}

	/**
	 * Calls setUpMap when map data loaded.
	 * @param googleMap
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		setUpMap();
	}

	/**
	 * Sets map style, ui settings and moves camera to last address or, if it first to current
	 * location (if permission granted).
	 * Also set listener to wrapper that update text presentation of current address when each move
	 * ended.
	 */
	private void setUpMap() {
		if (googleMap == null)
			return;

		googleMap.setMapStyle(
				MapStyleOptions.loadRawResourceStyle(
						LocationSelectActivity.this, R.raw.google_map_style));
		googleMap.setMaxZoomPreference(20);

		if (hasGPSPermission())
			googleMap.setMyLocationEnabled(true);

		googleMap.setIndoorEnabled(false);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		googleMap.setTrafficEnabled(false);
		googleMap.getUiSettings().setAllGesturesEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(false);
		googleMap.getUiSettings().setMapToolbarEnabled(false);
		wrapper.setListener(() -> {

			final LatLng pos = googleMap.getCameraPosition().target;
			(new Thread() {
				@Override
				public void run() {
					final String address = getAddressFromLocation(pos);
					handler.post(() -> {
						if (address != null)
							addressInput.setText(address);
					});
				}
			}).start();
		});

		if (getIntent().hasExtra("lastAddress")) {
			addressInput.setText(getIntent().getStringExtra("lastAddress"));
			double latitude = getIntent().getDoubleExtra("lat", 0);
			double longitude = getIntent().getDoubleExtra("lon", 0);
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));
		} else {
			if (hasGPSPermission()) {
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getMyPosition(), 17));
			} else {
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 9));
			}
		}
	}

	/**
	 * Returns current position by GPS.
	 */
	private LatLng getMyPosition() {
		if (!hasGPSPermission()) return null;

		String locationProvider = LocationManager.GPS_PROVIDER;
		//permission checked above
		@SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		double myLatitude = lastKnownLocation.getLatitude();
		double myLongitude = lastKnownLocation.getLongitude();
		return new LatLng(myLatitude, myLongitude);
	}

	/**
	 * Checks if location permission granted.
	 */
	private boolean hasGPSPermission() {
		return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION));
	}
}
