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
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.utils.NetworkUtils;

/**
 * Created by nas2skupa on 26/04/16.
 */
@EFragment(R.layout.fragment_register)
public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";

    @ViewById
    ViewGroup layoutMain;
    @ViewById(R.id.layout_display_name)
    TextInputLayout layoutDisplayName;
    @ViewById(R.id.text_display_name)
    EditText textDisplayName;
    @ViewById(R.id.layout_email)
    TextInputLayout layoutEmail;
    @ViewById(R.id.text_email)
    EditText textEmail;
    @ViewById(R.id.layout_password)
    TextInputLayout layoutPassword;
    @ViewById(R.id.text_password)
    EditText textPassword;
    @ViewById(R.id.button_register)
    Button buttonRegister;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    public void setTextEmail(@ViewById(R.id.text_email) EditText textEmail) {
        if (getArguments() == null) return;
        String email = getArguments().getString("email");
        if (email != null) textEmail.setText(email);
    }

    @EditorAction(R.id.text_password)
    public boolean signInKeyboard() {
        buttonRegister.callOnClick();
        return true;
    }

    @TextChange({R.id.text_email, R.id.text_password})
    public void clearErrors() {
        layoutDisplayName.setError(null);
        layoutDisplayName.setErrorEnabled(false);
        layoutEmail.setError(null);
        layoutEmail.setErrorEnabled(false);
        layoutPassword.setError(null);
        layoutPassword.setErrorEnabled(false);
    }

    @Click(R.id.button_register)
    public void register() {
        hideKeyboard();

        clearErrors();

        final String name = textDisplayName.getText().toString();
        final String email = textEmail.getText().toString();
        final String password = this.textPassword.getText().toString();
        final boolean isValidEmail = email.matches("^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$");

        if (name.length() < 3) {
            layoutDisplayName.setError(getString(R.string.msg_register_invalid_display_name));
            layoutDisplayName.setErrorEnabled(true);
            textDisplayName.requestFocus();
            return;
        } else if (!isValidEmail) {
            layoutEmail.setError(getString(R.string.msg_register_invalid_email));
            layoutEmail.setErrorEnabled(true);
            textEmail.requestFocus();
            return;
        } else if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.msg_register_invalid_password));
            layoutPassword.setErrorEnabled(true);
            textPassword.requestFocus();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            Snackbar.make(layoutMain, R.string.msg_register_no_network, Snackbar.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful() || FirebaseAuth.getInstance().getCurrentUser() == null) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Snackbar.make(layoutMain, R.string.msg_register_user_already_exists, Snackbar.LENGTH_INDEFINITE)
                                        .setAction(R.string.msg_register_user_already_exists_action, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Bundle args = new Bundle();
                                                args.putString("email", email);
                                                ForgotPasswordFragment_ fragment = new ForgotPasswordFragment_();
                                                fragment.setArguments(args);
                                                getActivity().getSupportFragmentManager().beginTransaction()
                                                        .replace(R.id.fragment_container, fragment, "ForgotPasswordFragment")
                                                        .addToBackStack("ForgotPasswordFragment")
                                                        .commit();
                                            }
                                        }).show();
                            }
                            else {
                                Snackbar.make(layoutMain, R.string.msg_register_error, Snackbar.LENGTH_LONG).show();
                            }
                            return;
                        }

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });

                        startActivity(new Intent(getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(layoutMain.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
