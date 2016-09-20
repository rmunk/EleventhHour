package hr.nas2skupa.eleventhhour.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.User;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.ui.MainActivity_;
import hr.nas2skupa.eleventhhour.utils.NetworkUtils;

/**
 * Created by nas2skupa on 26/04/16.
 */
@EFragment(R.layout.fragment_sign_in)
public class SignInFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignInFragment";
    private static final int RC_SIGN_IN = 1001;

    @ViewById
    ViewGroup layoutMain;

    @ViewById(R.id.layout_email)
    TextInputLayout layoutEmail;
    @ViewById(R.id.text_email)
    EditText textEmail;
    @ViewById(R.id.layout_password)
    TextInputLayout layoutPassword;
    @ViewById(R.id.text_password)
    EditText textPassword;

    @ViewById(R.id.button_sign_in)
    Button buttonSignIn;
    @ViewById(R.id.button_google_plus)
    ImageView buttonGoogle;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @EditorAction(R.id.text_password)
    public boolean signInKeyboard() {
        buttonSignIn.callOnClick();
        return true;
    }

    @TextChange({R.id.text_email, R.id.text_password})
    public void clearErrors() {
        layoutEmail.setError(null);
        layoutEmail.setErrorEnabled(false);
        layoutPassword.setError(null);
        layoutPassword.setErrorEnabled(false);
    }

    @Click(R.id.button_sign_in)
    public void signIn() {
        hideKeyboard();

        clearErrors();

        final String email = textEmail.getText().toString();
        final String password = this.textPassword.getText().toString();
        final boolean isValidEmail = email.matches("^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$");

        if (!isValidEmail) {
            layoutEmail.setError("Please enter valid email address.");
            layoutEmail.setErrorEnabled(true);
            textEmail.requestFocus();
            return;
        } else if (password.length() < 6) {
            layoutPassword.setError("Password must be at least 6 characters long");
            layoutPassword.setErrorEnabled(true);
            textPassword.requestFocus();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            Snackbar.make(layoutMain, R.string.msg_sign_in_no_network, Snackbar.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        //TODO: Login
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Snackbar.make(layoutMain, R.string.msg_sign_in_wrong_credentials, Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        onAuthSuccess(task.getResult().getUser());
                    }
                });
    }

    @Click(R.id.button_register)
    public void register() {
        hideKeyboard();

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment_(), "RegisterFragment")
                .addToBackStack("RegisterFragment")
                .commit();
    }

    @Click(R.id.button_forgot_password)
    public void forgotPassword() {
        hideKeyboard();

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ForgotPasswordFragment_(), "ForgotPasswordFragment")
                .addToBackStack("ForgotPasswordFragment")
                .commit();
    }

    @Click(R.id.button_google_plus)
    public void signInGoogle() {
        hideKeyboard();
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Click(R.id.button_facebook)
    public void signInFacebook() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Click(R.id.button_twitter)
    public void signInTwitter() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(layoutMain, R.string.msg_sign_in_google_failed, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        progressBar.setVisibility(View.GONE);

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Snackbar.make(layoutMain, R.string.msg_sign_in_google_failed, Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        onAuthSuccess(task.getResult().getUser());
                    }
                });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(layoutMain.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void onAuthSuccess(FirebaseUser firebaseUser) {
        // Write the new user
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        User user = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getPhotoUrl());
        database.child("users").child(firebaseUser.getUid()).setValue(user);

        // Go to MainActivity
        MainActivity_.intent(getContext())
                .action(MainActivity.HOME)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buttonGoogle.setVisibility(View.GONE);
    }


}