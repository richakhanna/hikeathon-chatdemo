package hikeathon.com.hikeathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//import android.util.Log;

public class NetworkUtils {

	// private static String LOG_TAG = NetworkUtils.class.getSimpleName();

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		boolean isOnline = netInfo != null && netInfo.isConnected();

		// Log.v(LOG_TAG, "network available: " + isOnline);

		if (isOnline) {
			Process pingProcess = null;
			try {
				// start execution
				pingProcess = new ProcessBuilder()
						.command("/system/bin/ping", "-c1", "8.8.8.8")
						.redirectErrorStream(true).start();
				// Log.v(LOG_TAG, "ping process started execution");

				// exhaust input stream
				StreamReader reader = new StreamReader(
						pingProcess.getInputStream());
				reader.start();
				// Log.v(LOG_TAG, "streamReader thread started");

				// wait for completion
				int exitValue = pingProcess.waitFor();
				// To handle condition where the process ends before the threads
				// finish
				if (reader.isAlive()) {
					reader.join();
				}
				// Log.v(LOG_TAG, "exitValue: " + exitValue);

				isOnline = exitValue == 0;
				// Log.v(LOG_TAG, "internet available: " + isOnline);

			} catch (Exception e) {
				// Log.e(e.getMessage(), e.toString());
				isOnline = false;

			} finally {
				if (null != pingProcess) {
					// Log.d(LOG_TAG, "destroying ping process");
					pingProcess.destroy();
				}

			}
		}
		// Log.v(LOG_TAG, "returning " + isOnline + " as isOnline");
		return isOnline;
	}
}

class StreamReader extends Thread {
	private BufferedReader reader;

	public StreamReader(InputStream is) {
		this.reader = new BufferedReader(new InputStreamReader(is));
	}

	public void run() {
		try {
			@SuppressWarnings("unused")
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Log.v("StreamReader: ", line);
			}

		} catch (IOException e) {
			// Log.e(e.getMessage(), e.toString());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				// Log.e(e.getMessage(), e.toString());
			}
		}
	}
}
