package org.beiwe.app.networking;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.beiwe.app.storage.PersistentData;

public class FCMInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FirebaseInstanceIDServ", "Refreshed token: " + refreshedToken);
        PersistentData.setFCMInstanceID(refreshedToken);
    }
}
