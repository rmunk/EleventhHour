package hr.nas2skupa.eleventhhour.events;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by nas2skupa on 05/10/2016.
 */

public class AuthSuccessfulEvent {
    FirebaseUser firebaseUser;

    public AuthSuccessfulEvent(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }
}
