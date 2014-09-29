package org.beiwe.app.networking;

import java.io.IOException;
import java.net.URL;


import android.os.AsyncTask;
import android.util.Log;

public class AsyncPostSender extends AsyncTask<String, String, String> {

	@Override
	protected String doInBackground(String... params) {
		try {
			String response = PostRequestFileUpload.sendPostRequest(new URL(params[0]));
			Log.i("AsyncPostSender", "RESPONSE = " + response);
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("AsyncPostSender", "IOError");
			System.exit(1);
		}
		return "Error Occured";
	}
}
