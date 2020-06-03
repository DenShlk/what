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

package com.hypersphere.what.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.hypersphere.what.R;
import com.hypersphere.what.helpers.CloudHelper;
import com.hypersphere.what.model.ProjectEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment with map, navigation ui, and bottom sheet. Last one used
 * to show {@link ProjectInfoFragment} on project marker click.
 * Adds markers for each project with it's name and complete-level.
 */
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback, LocationListener {

	private static final int LOCATION_REQUEST = 473;
	private static final String[] LOCATION_PERMS = {
			Manifest.permission.ACCESS_FINE_LOCATION
	};

	private GoogleMap googleMap;
	private View mView;

	//Saint-Petersburg
	private LatLng defaultLocation = new LatLng(59.940805, 30.344595);

	private MaterialCardView infoCard;
	private boolean focusedOnMarker = false;

	private LocationManager locationManager;

	private final Map<Marker, ProjectEntry> markerProjectMap = new HashMap<>();

	private List<ProjectEntry> projects;

	private ProjectInfoFragment infoFragment;

	private BottomSheetBehavior<RelativeLayout> infoBehaviour;

	public GoogleMapFragment() {
		//required
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_google_map, container, false);

		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		preSetUpMap();

		setUpButtons();

		infoFragment = new ProjectInfoFragment();

		infoFragment.setCloseListener(() -> {
			focusedOnMarker = false;
			infoBehaviour.setHideable(true);
			infoBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
		});

		infoCard = mView.findViewById(R.id.info_card);

		getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.info_frame, infoFragment).commit();

		final ConstraintLayout controlsLayout = mView.findViewById(R.id.controls_layout);
		final RelativeLayout relativeLayout = mView.findViewById(R.id.bottom_sheet);
		infoBehaviour = (BottomSheetBehavior<RelativeLayout>) ((CoordinatorLayout.LayoutParams) relativeLayout.getLayoutParams()).getBehavior();
		infoBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
		infoBehaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams) controlsLayout.getLayoutParams());
				// [hard math] Long experiments have shown that it is only true way to smooth move up
				int margin = (int) (slideOffset * relativeLayout.getHeight() + (1 - slideOffset) * infoBehaviour.getPeekHeight());
				if (margin < 0)
					margin = 0;
				params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, margin);
				controlsLayout.setLayoutParams(params);
			}
		});

		return mView;
	}

	/**
	 * Hides bottom sheet because after onPause() project info fragment is empty.
	 */
	@Override
	public void onResume() {
		super.onResume();
		infoBehaviour.setHideable(true);
		infoBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
		infoCard.setVisibility(View.GONE);
	}

	/**
	 * Sets up google map navigation ui.
	 * Runnable used to loop zoom action if button stay pressed.
	 */
	private void setUpButtons() {
		mView.findViewById(R.id.my_position_button).setOnClickListener(v -> {
			if (hasGPSPermission())
				googleMap.animateCamera(CameraUpdateFactory.newLatLng(getMyPosition()));
			else
				googleMap.animateCamera(CameraUpdateFactory.newLatLng(defaultLocation));
		});

		final Handler handler = new Handler();

		final View zoomInButton = mView.findViewById(R.id.zoom_in_button);
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

		final View zoomOutButton = mView.findViewById(R.id.zoom_out_button);
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
	 * Starts map loading and requests permission.
	 */
	private void preSetUpMap() {
		SupportMapFragment googleMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
		googleMapFragment.getMapAsync(this);

		if (!hasGPSPermission()) {
			requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == LOCATION_REQUEST) {
			setUpMap();
		}
	}

	/**
	 * Loads markers when map is ready. Sets camera move listeners.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		setUpMap();
		CloudHelper.loadProjects(new CloudHelper.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				projects = data;
				for (ProjectEntry project : projects) {
					addProjectMarker(project);
				}
			}

			@Override
			public void onCancel() {
			}
		});

		// show bottom sheet with clicked marker info
		googleMap.setOnMarkerClickListener(marker -> {

			final ProjectEntry project = markerProjectMap.get(marker);
			infoFragment.fillInfo(project);

			if (!focusedOnMarker)
				changeFocusedOnMarker();

			return false;
		});

		//hide bottom sheet if camera moved or user click free map space
		googleMap.setOnCameraMoveStartedListener(i -> {
			if (i == 1) {// move from user
				if (focusedOnMarker)
					changeFocusedOnMarker();
			}
		});
		googleMap.setOnMapClickListener(latLng -> {
			if (focusedOnMarker)
				changeFocusedOnMarker();
		});

	}

	/**
	 * Focuses on marker or cancel it.
	 */
	private void changeFocusedOnMarker() {
		focusedOnMarker = !focusedOnMarker;

		if (focusedOnMarker) {
			//show bottom sheet
			infoCard.setVisibility(View.VISIBLE);

			if (infoBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN){
				infoBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
			}

			infoBehaviour.setHideable(false);
			infoBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

		} else {
			//move bottom sheet to minimal visibility position
			infoBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
		}
	}

	/**
	 * Sets google map ui and move camera to user location (if has permission) or to default location.
	 */
	@SuppressLint("MissingPermission")
	private void setUpMap() {
		if (googleMap == null)
			return;

		googleMap.setMapStyle(
				MapStyleOptions.loadRawResourceStyle(
						getContext(), R.raw.google_map_style));

		googleMap.setMaxZoomPreference(20);

		googleMap.setIndoorEnabled(false);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		googleMap.setTrafficEnabled(false);
		googleMap.getUiSettings().setAllGesturesEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(false);
		googleMap.getUiSettings().setMapToolbarEnabled(false);

		//permission checked
		if (hasGPSPermission()) {
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyPosition(), 16));
			googleMap.setMyLocationEnabled(true);
		} else {
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 9));
		}
	}

	/**
	 * Returns current user position.
	 * @return
	 */
	private LatLng getMyPosition() {
		if (!hasGPSPermission()) return null;

		List<String> providers = locationManager.getProviders(true);
		Location bestLocation = null;
		for (String provider : providers) {
			//permission checked above
			@SuppressLint("MissingPermission") Location loc = locationManager.getLastKnownLocation(provider);
			if (loc == null) {
				continue;
			}
			if (bestLocation == null || loc.getAccuracy() < bestLocation.getAccuracy()) {
				bestLocation = loc;
			}
		}

		double myLatitude = bestLocation.getLatitude();
		double myLongitude = bestLocation.getLongitude();
		return new LatLng(myLatitude, myLongitude);
	}

	/**
	 * Checks if GPS permission granted. Returns false if android version is too low
	 * @return
	 */
	private boolean hasGPSPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return (PackageManager.PERMISSION_GRANTED == getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION));
		} else return false;
	}

	/**
	 * Creates marker for given project and adds it on map.
	 * @param project
	 */
	private void addProjectMarker(ProjectEntry project){
		View markerView = ((LayoutInflater) getActivity()
				.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.project_marker_layout, null);

		View progressView = markerView.findViewById(R.id.marker_progress_image);

		//max height of image
		float maxHeight = getResources().getDimension(R.dimen.marker_progress_drawable_height);
		progressView.getLayoutParams().height = (int) (maxHeight * (1 - project.donationsCollected / project.donationsGoal));
		progressView.requestLayout();

		TextView titleText = markerView.findViewById(R.id.marker_title);
		titleText.setText(project.title);

		Marker marker = googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(project.latitude, project.longitude))
				.icon(BitmapDescriptorFactory
						.fromBitmap(createDrawableFromView(
								markerView))));

		markerProjectMap.put(marker, project);
	}

	/**
	 * Draws marker of given project on bitmap.
	 * @param view
	 * @return bitmap shows given project
	 */
	private Bitmap createDrawableFromView(View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.layout(0, 0, displayMetrics.widthPixels,
				displayMetrics.heightPixels);
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
				view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);

		return bitmap;
	}

	/**
	 * Methods below implements {@link OnMapReadyCallback} and haven't realized yet.
	 */

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}
}

