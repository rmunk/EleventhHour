package hr.nas2skupa.eleventhhour.auth;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startApplication();
        } else {
            startSignInProcess();
        }
    }

    private void startSignInProcess() {
        startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
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
            if (resultCode == ResultCodes.OK && auth.getCurrentUser() != null) {
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
                    Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
        }
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
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
        }
    }
}