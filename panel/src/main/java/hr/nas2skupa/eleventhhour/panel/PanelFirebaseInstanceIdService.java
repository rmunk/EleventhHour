package hr.nas2skupa.eleventhhour.panel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

/**
 * Created by nas2skupa on 06/04/2017.
 */

public class PanelFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        Timber.i("Notification token refreshed.");

        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && MainActivity.providerKey != null && token != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("app/notificationTokens/panel")
                    .child(MainActivity.providerKey)
                    .child(token)
                    .setValue(true);
        }
    }
}
