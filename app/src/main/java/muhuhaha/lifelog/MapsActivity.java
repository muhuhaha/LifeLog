package muhuhaha.lifelog;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationReceiver.Receiver {
	private static final String TAG = "LifeLog_Map";
	private final float FAST_SPEED = 3;

	private GoogleMap mMap;
	private PolylineOptions polylineOptions;
	private LocationReceiver mReceiver;
	private boolean mFirstLocation = false;
	private BackPressCloseHandler backPressCloseHandler;

	//    private static int markerSequence;
	//    String mMarkerString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "[onCreate] ... " + LocationCollector.mIsServiceRunning);

		super.onCreate(savedInstanceState);

		//show error dialog if GoolglePlayServices not available
		if (!isGooglePlayServicesAvailable()) {
			finish();
		}

		setContentView(R.layout.activity_maps);

		// 지나간 길은 보여줘야지
		polylineOptions = new PolylineOptions();
		backPressCloseHandler = new BackPressCloseHandler(this);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		Log.d(TAG, "[onCreate] newnewnew!");

		// Location 정보를 받을 connection
		mReceiver = new LocationReceiver(new Handler());
		mReceiver.setReceiver(this);
		// markerSequence = 0;

		Log.d(TAG, "[onCreate] newnewnew!");

		// 살아있는지 물어보지 않는다
//		if (!LocationCollector.mIsServiceRunning) {
			final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, LocationCollector.class);
			intent.setAction("construct");
			intent.putExtra("receiver", mReceiver);
			startService(intent);
//		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		Log.d(TAG, "[onMapReady] Map is ready");

		mMap = googleMap;
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);

//		Polyline polyline = mMap.addPolyline(polylineOptions.width(12).color(Color.GREEN).geodesic(true));
//		Log.d(TAG, "[addLocationInfo] addPolyline " + polyline.getPoints().size());
	}

	private void addLocationInfo(Location mCurrentLocation) {
		//MarkerOptions options = new MarkerOptions();
		//markerSequence++;

/*
		IconGenerator iconFactory = new IconGenerator(this);
		iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
		options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mMarkerString)));
		options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
*/
		if (mCurrentLocation == null) {
			Log.d(TAG, "[addLocationInfo] location is null!");
			Toast.makeText(this, "No Location!", Toast.LENGTH_LONG).show();
		}
		else {
			Log.d(TAG, "[addLocationInfo] let's move to center");
			LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

			if (mFirstLocation) {
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
				mFirstLocation = false;
			}
			else
				mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

			Log.d(TAG, "[addLocationInfo] Move to current position");

			polylineOptions.add(currentLatLng);
			Polyline polyline = mMap.addPolyline(polylineOptions.width(12).color(Color.GREEN).geodesic(true));
			Log.d(TAG, "[addLocationInfo] addPolyline " + polyline.getPoints().size());

			//options.position(currentLatLng);
			//Marker mapMarker = mMap.addMarker(options);
			//mMarkerString = markerSequence + "";
			//mapMarker.setTitle(mMarkerString);
			//Log.d(TAG, "[addMarker] Marker added at " + mCurrentLocation.getLatitude() + "::" + mCurrentLocation.getLongitude());
		}
	}

	private void addLocationInfo(LatLng mCurrentLocation) {
		if (mCurrentLocation == null) {
			Log.d(TAG, "[addLocationInfo] location is null!");
			Toast.makeText(this, "No Location!", Toast.LENGTH_LONG).show();
		}
		else {
			LatLng currentLatLng = mCurrentLocation;

			if (mFirstLocation) {
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
				mFirstLocation = false;
			}
			else
				mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

			Log.d(TAG, "[addLocationInfo] previous location " + currentLatLng.latitude + ", " + currentLatLng.longitude);

			polylineOptions.add(currentLatLng);
			Polyline polyline = mMap.addPolyline(polylineOptions.width(12).color(Color.GREEN).geodesic(true));
			Log.d(TAG, "[addLocationInfo] Previously addPolyline " + polyline.getPoints().size());
		}
	}

	private int calculateArea(PolylineOptions polylineOptions) {
		int zoomNumber = 5;

		// 모든 점과 line이 표시되려면...
		// marker 크기도 변경이 가능할까?

		return zoomNumber;
	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
			case LocationCollector.RESULT_LOCATION:
				Location location = resultData.getParcelable("location");
				addLocationInfo(location);
				break;
			case LocationCollector.RESULT_FIRSTTIME:
				mFirstLocation = true;
				break;
			case LocationCollector.RESULT_PREVIOUS:
				LatLng latlng = resultData.getParcelable("prevLocation");
				addLocationInfo(latlng);
				break;
			default:
				break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Life cycle management

	@Override
	protected void onStart() {
		Log.d(TAG, "[onStart] onStarting...");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "[onStop] onStop...");
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "[onBackPressed] back pressed!!!");
//
//		final Intent intent = new Intent();
//		intent.setAction("store");
//		sendBroadcast(intent);

		backPressCloseHandler.onBackPressed();
	}
}
