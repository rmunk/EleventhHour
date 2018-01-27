package hr.nas2skupa.eleventhhour.auth;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class SignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            saveUserInfo(auth.getCurrentUser());
            startApplication();
        } else {
            startSignInProcess();
        }
    }

    private void startSignInProcess() {
        startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.PhoneBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
//                                new AuthUI.IdpConfig.TwitterBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setLogo(R.drawable.logo)
                        .setTheme(R.style.SignInTheme)
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK && auth.getCurrentUser() != null) {
                FirebaseUser user = auth.getCurrentUser();

                if (Fabric.isInitialized()) {
                    Crashlytics.setUserIdentifier(user.getUid());
                    Crashlytics.setUserName(user.getDisplayName());
                    Crashlytics.setUserEmail(user.getEmail());
                }

                saveUserInfo(user);
                startApplication();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    finish();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Timber.e("Login failed (UNKNOWN_ERROR)");
                    Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Timber.e("Login failed (No error code)");
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
        }
    }

    private void saveUserInfo(FirebaseUser currentUser) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", currentUser.getDisplayName());
        userMap.put("email", currentUser.getEmail());
        if (currentUser.getPhotoUrl() != null) {
            userMap.put("photoUrl", currentUser.getPhotoUrl().toString());
        }
        FirebaseDatabase.getInstance().getReference()
                .child("users/data")
                .child(currentUser.getUid())
                .updateChildren(userMap)
                .addOnFailureListener(e -> Timber.e(e, "Failed to save user data to database"));
    }

    private void startApplication() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String entryPoint = bundle.getString("app_entry_point");
            if (entryPoint != null) {
                Intent intent = new Intent(entryPoint);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                throw new IllegalStateException("You must configure <meta-data android:name=\\\"app_entry_point\\\" android:value=\\\" main entry intent action\\\"/> in your AndroidManifest.xml file.\"");
            }
        } catch (Exception e) {
            Timber.e("Login failed (Could not start main app)", e);
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
        }
    }
}
