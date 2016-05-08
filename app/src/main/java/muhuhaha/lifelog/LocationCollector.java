package muhuhaha.lifelog;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import muhuhaha.lifelog.data.FileStore;
import muhuhaha.lifelog.data.ILocationStore;
import muhuhaha.lifelog.data.LocationInfo;

/**
 * Created by muhuhaha on 2015-11-07.
 */
public class LocationCollector extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
	public static boolean mIsServiceRunning = false;
	public static boolean mIsPreviousLocation = false;
	private static final String TAG = "LifeLog_LocationCollector";
	protected GoogleApiClient mGoogleApiClient;
	LocationRequest mLocationRequest;
	ResultReceiver mConnectionToActivity = null;

	private static final long INTERVAL_LONG = 1000 * 60 * 3; // 3 minutes
	private static final long INTERVAL_SHORT = 1000 * 1; // 1 sec
	private static final long INTERVAL_MEDIUM = 1000 * 60; // 1 minute
	private static final long DISPLACEMENT = 10;

	public static final int RESULT_LOCATION = 0;
	public static final int RESULT_FIRSTTIME = 1;
	public static final int RESULT_PREVIOUS = 2;
	public static final int RESULT_BBB = 3;

	private List<LocationInfo> mLocationList = new ArrayList<LocationInfo>();
	private List<LatLng> mPrevLocationList = new ArrayList<LatLng>();
	private Location mLastLocation;
	private ILocationStore mFileStore;
	private float mTotalDistance = 0;
	private int mIndex = 0;

	public LocationCollector() {
		super("LocationCollector");
		Log.d(TAG, "[LocationCollector] constructed!");

		mFileStore = FileStore.getInstance();
	}

	/**
	 * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
	 */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "[onHandleIntent] received!");

		String action = intent.getAction();
		mConnectionToActivity = intent.getParcelableExtra("receiver");
		Log.d(TAG, "[onHandleIntent] " + action);

		// 새롭게 서비스를 시작할때
		if (action.equals("construct")) {
			buildGoogleApiClient();
			mGoogleApiClient.connect();

			Log.d(TAG, "[onHandleIntent] " + mPrevLocationList.size());
			mPrevLocationList = mFileStore.readLocation();
			Log.d(TAG, "[onHandleIntent] " + mPrevLocationList.size());

			for (LatLng location : mPrevLocationList) {
				Bundle bundle = new Bundle();
				bundle.putParcelable("prevLocation", location);
				mConnectionToActivity.send(RESULT_PREVIOUS, bundle);
			}

			Log.d(TAG, "[onHandleIntent] " + action);

			createLocationRequest();
		}

		mIsServiceRunning = true;
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(INTERVAL_LONG);
		mLocationRequest.setFastestInterval(INTERVAL_SHORT);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	public void onStop() {
		Log.d(TAG, "[onStop] Location store...");

		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	public void onDestroy() {
		Log.d(TAG, "[onDestroy] Location Collector destroyed...");
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "[onLocationChanged] Location changed!");

		boolean isAvailableLocation = manageLocation(location);

		if (isAvailableLocation) {
			sendLocation(location);
		}
	}

	private void sendLocation(Location location) {
		Bundle bundle = new Bundle();
		bundle.putParcelable("location", location);
		mConnectionToActivity.send(RESULT_LOCATION, bundle);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
	}

	protected void startLocationUpdates() {
		PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
		Log.d(TAG, "[startLocationUpdates] location update started");
	}

	private boolean manageLocation(Location mCurrentLocation) {
		Log.d(TAG, "[manageLocation] location in managing");
		boolean result = false;

		// for the first time
		if (mLastLocation == null && mCurrentLocation != null) {
			Log.d(TAG, "[manageLocation] first location received");
			result = true;

			Bundle b = new Bundle();
			mConnectionToActivity.send(RESULT_FIRSTTIME, b);
		}

		// for the second and more times
		if (mLastLocation != null && mCurrentLocation != null) {
			Log.d(TAG, "[manageLocation] other location received");
			float distance = mCurrentLocation.distanceTo(mLastLocation);
			mTotalDistance += distance;

			result = true;
/*
			// 일단 180km 이하로 이동한 경우만 처리한다. 위치 튀는 것 방지.
			if (distance > 10 && distance < 1000) {
				Log.d(TAG, "[isMoving] distance between 2 points : " + distance);
				mTotalDistance += distance;
				return true;
			}
*/
		}

		if (result) {
			Log.d(TAG, "[manageLocation] location is stored");

			mLastLocation = mCurrentLocation;

			LocationInfo locationInfo = new LocationInfo(mIndex++, mLastLocation, mTotalDistance);
			mLocationList.add(locationInfo);

			mFileStore.storeLocation(locationInfo);
		}

		return result;
	}

	@Override
	public void onConnected(Bundle bundle) {
		// Provides a simple way of getting a device's location and is well suited for
		// applications that do not require a fine-grained location and that do not need location
		// updates. Gets the best and most recent location currently available, which may be null
		// in rare cases when a location is not available.
		Log.d(TAG, "[onConnected] onConnected!");
		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}

}
