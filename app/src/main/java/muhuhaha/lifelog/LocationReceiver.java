package muhuhaha.lifelog;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.Handler;

/**
 * Created by muhuhaha on 2015-11-08.
 */
public class LocationReceiver extends ResultReceiver {
	private Receiver mReceiver;

	public LocationReceiver(Handler handler) {
		super(handler);
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			mReceiver.onReceiveResult(resultCode, resultData);
		}
	}
}
