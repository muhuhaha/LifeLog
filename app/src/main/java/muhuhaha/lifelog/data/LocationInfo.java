package muhuhaha.lifelog.data;

import android.location.Location;

/**
 * Created by muhuhaha on 2016-05-08.
 */
public class LocationInfo {
	private int mIndex;
	private Location mLocation;
	private float mTotalDistance;

	public LocationInfo(int mIndex, Location mLocation, float mTotalDistance) {
		this.mIndex = mIndex;
		this.mLocation = mLocation;
		this.mTotalDistance = mTotalDistance;
	}

	public int getIndex() {
		return mIndex;
	}

	public Location getLocation() {
		return mLocation;
	}

	public float getTotalDistance() {
		return mTotalDistance;
	}

	public void setIndex(int index) {
		this.mIndex = index;
	}

	public void setLocation(Location location) {
		this.mLocation = location;
	}

	public void setTotalDistance(float totalDistance) {
		this.mTotalDistance = totalDistance;
	}
}
