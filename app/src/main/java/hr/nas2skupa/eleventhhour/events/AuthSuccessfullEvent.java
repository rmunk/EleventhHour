package hr.nas2skupa.eleventhhour.events;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by nas2skupa on 05/10/2016.
 */

public class AuthSuccessfullEvent {
    FirebaseUser firebaseUser;

    public AuthSuccessfullEvent(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }
}
