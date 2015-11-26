package muhuhaha.lifelog;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by muhuhaha on 2015-11-24.
 */
public class LocationStore {
	private static final String TAG = "LifeLog_LocationStore";

	private static LocationStore mLocationStore;
	private File mPath;
	private String mFileName;
	private File file;
	private FileOutputStream fos;

	private LocationStore()  {
		mPath = Environment.getExternalStorageDirectory().getAbsoluteFile();
		mFileName = (Calendar.getInstance().get(Calendar.MONTH)+1)+"_"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+".txt";
	}

	public static LocationStore getInstance() {
		Log.d(TAG, "[getInstance] created!");

		if (mLocationStore == null) {
			mLocationStore = new LocationStore();
		}

		return mLocationStore;
	}

	public void writeFile(String location) {
		Log.d(TAG, "[writeFile] let's write in file!");
		File file = new File(mPath, mFileName);

		try {
			fos = new FileOutputStream(file, true);
			fos.write(location.getBytes());
			fos.close();
			Log.d(TAG, "[writeFile] done!");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "[writeFile] no! exception  "+e.toString());
		}
	}
}
