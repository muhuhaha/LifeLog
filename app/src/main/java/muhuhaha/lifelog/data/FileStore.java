package muhuhaha.lifelog.data;

import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by muhuhaha on 2015-11-24.
 */
public class FileStore implements ILocationStore {
	private static final String TAG = "LifeLog_LocationStore";

	private static FileStore mFileStore;
	private File mPath;
	private File file;
	private String mFileName;
	private FileOutputStream fos;
	private FileInputStream fis;

	private List<LatLng> locationList;

	private FileStore()  {
		// 오늘 날짜의 파일 이름을 만들거나 존재하면 읽어들인다
		mPath = Environment.getExternalStorageDirectory().getAbsoluteFile();
		mFileName = (Calendar.getInstance().get(Calendar.MONTH)+1)+"_"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+".txt";

		file = new File(mPath, mFileName);
	}

	public static FileStore getInstance() {
		Log.d(TAG, "[getInstance] created!");

		if (mFileStore == null) {
			mFileStore = new FileStore();
		}

		return mFileStore;
	}

	@Override
	public void storeLocation(List<LocationInfo> locationList) {
		Log.d(TAG, "[storeLocation] let's write in file!");

		try {
			fos = new FileOutputStream(file, true);
			fos.write(locationList.toString().getBytes());
			Log.d(TAG, "[storeLocation] " + locationList.toString());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "[storeLocation] no! exception  "+e.toString());
		}
	}

	@Override
	public void storeLocation(LocationInfo location) {
		Log.d(TAG, "[storeLocation] let's write in file!");
		String storedLocation = new String();

		// 결과 저장 formatting
		storedLocation += location.getLocation().getLatitude() + "::";
		storedLocation += location.getLocation().getLongitude() + "++";
		Log.d(TAG, "[storeLocation] " + storedLocation);

		try {
			fos = new FileOutputStream(file, true);
			fos.write(storedLocation.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "[storeLocation] no! exception " + e.toString());
		}
	}

	@Override
	public List<LatLng> readLocation() {
		Log.d(TAG, "[readLocation] let's read location!");
		locationList = new ArrayList<>();

		try {
			fis = new FileInputStream(file);

			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
			String result = bufferReader.readLine();
			Log.d(TAG, "[readLocation] " + result + "\n");

			if (result != null) {
				StringTokenizer str1 = new StringTokenizer(result, "++");

				while (str1.hasMoreTokens()) {
					String latlng = str1.nextToken();
					StringTokenizer str2 = new StringTokenizer(latlng, "::");

					LatLng location = new LatLng(Double.parseDouble(str2.nextToken()), Double.parseDouble(str2.nextToken()));
					//Log.d(TAG, "[readLocation] " + location.latitude + ", " + location.longitude);

					locationList.add(location);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "[readFile] no! exception  " + e.toString());
		}

		return locationList;
	}
}
