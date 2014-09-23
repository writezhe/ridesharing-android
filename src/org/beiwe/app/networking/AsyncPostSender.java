package org.beiwe.app.networking;

import java.io.IOException;
import java.net.URL;


import android.os.AsyncTask;
import android.util.Log;

public class AsyncPostSender extends AsyncTask<String, String, String> {

	@Override
	protected String doInBackground(String... params) {
		try {
			int response = PostRequestFileUpload.sendPostRequest(params[0], new URL(params[1]));
			String strResponse = "" + response;
			Log.i("AsyncPostSender", "RESPONSE = " + strResponse);
			return strResponse;
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("AsyncPostSender", "IOError");
			System.exit(1);
		}
		return "Error Occured";
	}
}
