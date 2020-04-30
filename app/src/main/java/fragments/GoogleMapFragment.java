package fragments;


import android.Manifest;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.Toast;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hypersphere.what.CloudManager;
import com.hypersphere.what.ProjectEntry;
import com.hypersphere.what.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoogleMapFragment extends Fragment implements OnMapReadyCallback, LocationListener {

	private static final int LOCATION_REQUEST = 473;
	private static final String[] LOCATION_PERMS = {
			Manifest.permission.ACCESS_FINE_LOCATION
	};
	private boolean waitPermissionForMap = false;

	private GoogleMap googleMap;
	private View mView;
	private double myLatitude;
	private double myLongitude;

	private MaterialCardView infoCard;
	private AnimatorSet infoCardAnimationSet = new AnimatorSet();
	private boolean focusedOnMarker = false;

	private LocationManager locationManager;
	private String locationProvider = LocationManager.GPS_PROVIDER;

	private Map<Marker, ProjectEntry> markerProjectMap = new HashMap<>();

	private List<ProjectEntry> projects;

	private MaterialButton donateButton;
	private ProjectInfoFragment infoFragment;

	private BottomSheetBehavior<RelativeLayout> infoBehaviour;

	public GoogleMapFragment() {
		//required
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_google_map, container, false);

		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		preSetUpMap();

		setUpButtons();

		infoFragment = new ProjectInfoFragment();

		infoCard = mView.findViewById(R.id.info_card);
		donateButton = mView.findViewById(R.id.info_donate_button);

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
				FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams)controlsLayout.getLayoutParams());
				params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, (int) (slideOffset * relativeLayout.getHeight()));
				controlsLayout.setLayoutParams(params);
			}
		});

		return mView;
	}

	private void setUpButtons(){
		mView.findViewById(R.id.my_position_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				googleMap.animateCamera(CameraUpdateFactory.newLatLng(getMyPosition()));
			}
		});

		final Handler handler = new Handler();

		final View zoomInButton = mView.findViewById(R.id.zoom_in_button);
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

		final View zoomOutButton = mView.findViewById(R.id.zoom_out_button);
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
			if (!hasGPSPermission()) {
				Toast.makeText(getContext(), "All right, then. Keep your secrets.", Toast.LENGTH_LONG).show();
				setUpMap();
			} else {
				if (waitPermissionForMap) {
					waitPermissionForMap = false;
					setUpMap();
				}
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
		if (!hasGPSPermission())
			waitPermissionForMap = true;
		else
			setUpMap();


		CloudManager.startIfNeed();
		CloudManager.loadProjects(new CloudManager.OnDownloadListener<List<ProjectEntry>>() {
			@Override
			public void onComplete(List<ProjectEntry> data) {
				projects = data;
				for(ProjectEntry project : projects){
					addProjectMarker(project);
				}
			}
		});

		googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO: 27.04.2020
				//load info from marker from project

				ProjectEntry project = markerProjectMap.get(marker);
				infoFragment.fillInfo(project);

				if(!focusedOnMarker)
					changeFocusedOnMarker();

				return false;
			}
		});

		googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
			@Override
			public void onCameraMoveStarted(int i) {
				if(i==1){// move from user
					if(focusedOnMarker)
						changeFocusedOnMarker();
				}
			}
		});
		googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				if(focusedOnMarker)
					changeFocusedOnMarker();
			}
		});

	}



	private void changeFocusedOnMarker(){
		focusedOnMarker = !focusedOnMarker;
		if(focusedOnMarker) {
			infoBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
			infoBehaviour.setHideable(false);
			donateButton.setTranslationY(infoBehaviour.getExpandedOffset());
			//bottomPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
		}else {
			infoBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
			//infoCard.callOnClick();
			//bottomPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
		}
	}

	private void setUpMap() {
		if (googleMap == null)
			return;

		googleMap.setMapStyle(
				MapStyleOptions.loadRawResourceStyle(
						getContext(), R.raw.google_map_style));


		googleMap.setMaxZoomPreference(20);


		googleMap.setIndoorEnabled(false);
		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		googleMap.setTrafficEnabled(false);
		googleMap.getUiSettings().setAllGesturesEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(false);
		googleMap.getUiSettings().setMapToolbarEnabled(false);

		if (hasGPSPermission())
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getMyPosition(), 16));


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
		return (PackageManager.PERMISSION_GRANTED == getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION));
	}

	private Marker addProjectMarker(ProjectEntry project){
		View markerView = ((LayoutInflater) getActivity()
				.getSystemService(
						getActivity().LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.project_marker_layout, null);

		View progressView = markerView.findViewById(R.id.marker_progress_image);
		//// TODO: 29.04.2020 maxSize to dimens
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		//40dp to px...
		float maxHeight = displayMetrics.density * 40;
		progressView.getLayoutParams().height = (int) (maxHeight * (1 - project.donationsCollected / project.donationsGoal));
		progressView.requestLayout();

		//ProgressBar projectProgress = markerView.findViewById(R.id.marker_progress);
		//projectProgress.setMax((int) (project.donationsGoal * 100));
		//projectProgress.setProgress((int) (project.donationsCollected * 100));

		TextView titleTextLeft = markerView.findViewById(R.id.marker_title_left);
		titleTextLeft.setText(project.title);
		TextView titleTextRight = markerView.findViewById(R.id.marker_title_right);
		titleTextRight.setText(project.title);

		//View bottomView = markerView.findViewById(R.id.marker_bottom_view);
		//if more then 60% completed
		//if(project.donationsCollected >= project.donationsGoal * 0.60)
		//	bottomView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.progressColor));

		Marker marker = googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(project.latitude, project.longitude))
				.icon(BitmapDescriptorFactory
						.fromBitmap(createDrawableFromView(
								markerView))));

		markerProjectMap.put(marker, project);

		return marker;
	}

	private Bitmap createDrawableFromView(View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
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

	@Override
	public void onLocationChanged(Location location) {
		// TODO: 27.04.2020
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

