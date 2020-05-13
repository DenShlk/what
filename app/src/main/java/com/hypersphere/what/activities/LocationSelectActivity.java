package com.hypersphere.what.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hypersphere.what.views.TouchableWrapper;

import java.io.IOException;
import java.util.List;

public class LocationSelectActivity extends AppCompatActivity implements OnMapReadyCallback {

	private static final int LOCATION_REQUEST = 473;
	private static final String[] LOCATION_PERMS = {
			Manifest.permission.ACCESS_FINE_LOCATION
	};
	private boolean waitPermissionForMap = false;

	private GoogleMap googleMap;
	private double myLatitude;
	private double myLongitude;

	private LocationManager locationManager;
	private String locationProvider = LocationManager.GPS_PROVIDER;

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
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, new Intent());
				finish();
			}
		});

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		preSetUpMap();

		setUpButtons();

		addressInput = findViewById(R.id.location_edit_text);

		findViewById(R.id.clear_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addressInput.setText("");
			}
		});

		searchButton = findViewById(R.id.search_button);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (addressInput.getText().length() > 0) {
					LatLng loc = getLocationFromAddress(String.valueOf(addressInput.getText()));
					if (loc != null) {
						googleMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
						addressInput.clearFocus();

						//hide keyboard
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(addressInput.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
		});

		addressInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE ||
						actionId == EditorInfo.IME_ACTION_GO ||
						actionId == EditorInfo.IME_ACTION_NEXT) {
					searchButton.callOnClick();
					return true;
				}
				return false;
			}
		});

		findViewById(R.id.accept_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("lat", googleMap.getCameraPosition().target.latitude);
				data.putExtra("lon", googleMap.getCameraPosition().target.longitude);
				data.putExtra("address", getAddressFromLocation(googleMap.getCameraPosition().target));
				setResult(Activity.RESULT_OK, data);
				finish();
			}
		});
	}

	private void setUpButtons(){
		findViewById(R.id.my_position_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				googleMap.animateCamera(CameraUpdateFactory.newLatLng(getMyPosition()));
			}
		});

		final Handler handler = new Handler();

		final View zoomInButton = findViewById(R.id.zoom_in_button);
		zoomInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				googleMap.animateCamera(CameraUpdateFactory.zoomIn());
			}
		});
		final Runnable zoomInAction = new Runnable() {
			@Override
			public void run() {
				googleMap.animateCamera(CameraUpdateFactory.zoomBy(0.3f), 100, null);
				handler.postDelayed(this, 100);
			}
		};
		zoomInButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handler.post(zoomInAction);
				return true;
			}
		});
		zoomInButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.removeCallbacks(zoomInAction);
					//return true;
				}
				return false;
			}
		});

		final View zoomOutButton = findViewById(R.id.zoom_out_button);
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				googleMap.animateCamera(CameraUpdateFactory.zoomOut());
			}
		});
		final Runnable zoomOutAction = new Runnable() {
			@Override
			public void run() {
				googleMap.animateCamera(CameraUpdateFactory.zoomBy(-0.3f), 100, null);
				handler.postDelayed(this, 100);
			}
		};
		zoomOutButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handler.post(zoomOutAction);
				return true;
			}
		});
		zoomOutButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.removeCallbacks(zoomOutAction);
					//return true;
				}
				return false;
			}
		});
	}

	public LatLng getLocationFromAddress(String strAddress) {

		Geocoder geocoder = new Geocoder(this);
		List<Address> address;
		LatLng p1 = null;

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

	public String getAddressFromLocation(LatLng location) {

		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses;
		LatLng p1 = null;

		try {
			addresses = geocoder.getFromLocation(location.latitude, location.longitude, 5);
			if (addresses == null) {
				return null;
			}
			Address address = addresses.get(0);
			address.getLatitude();
			address.getLongitude();

			String text = address.getAddressLine(0);
			for (int i = 1; i < address.getMaxAddressLineIndex(); i++) {
				text += ", " + address.getAddressLine(i);
			}

			return text;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void preSetUpMap() {
		SupportMapFragment googleMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
		googleMapFragment.getMapAsync(this);

		if (!hasGPSPermission()) {
			requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == LOCATION_REQUEST) {
			if (!hasGPSPermission()) {
				Toast.makeText(LocationSelectActivity.this, "All right, then. Keep your secrets.", Toast.LENGTH_LONG).show();
				setUpMap();
			} else {
				setUpMap();

			}
		}
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		setUpMap();
	}

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


		wrapper.setListener(new TouchableWrapper.onCameraMoveEndListener() {
			@Override
			public void onCameraMoveEnd() {

				final LatLng pos = googleMap.getCameraPosition().target;
				(new Thread() {
					@Override
					public void run() {
						final String address = getAddressFromLocation(pos);
						handler.post(new Runnable() {
							@Override
							public void run() {
								if (address != null)
									addressInput.setText(address);
							}
						});
					}
				}).start();
			}
		});

		if (getIntent().hasExtra("lastAddress")) {
			addressInput.setText(getIntent().getStringExtra("lastAddress"));
			double latitude = getIntent().getDoubleExtra("lat", 0);
			double longitude = getIntent().getDoubleExtra("lon", 0);
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));
		} else {
			if (hasGPSPermission())
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getMyPosition(), 17));
		}
	}

	private LatLng getMyPosition() {
		if (!hasGPSPermission())
			return null;

		@SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		myLatitude = lastKnownLocation.getLatitude();
		myLongitude = lastKnownLocation.getLongitude();
		return new LatLng(myLatitude, myLongitude);
	}

	private boolean hasGPSPermission() {
		return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION));
	}
}
