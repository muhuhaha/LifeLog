package muhuhaha.lifelog.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by muhuhaha on 2016-05-08.
 */
public interface ILocationStore {
	// 리스트 내 location 리스트를 저장소에 저장한다
	void storeLocation(List<LocationInfo> locationList);

	// 리스트 내 location 하나를 저장소에 저장한다
	void storeLocation(LocationInfo location);

	// 저장소에 있는 location 정보를 리턴한다
	List<LatLng> readLocation();
}
