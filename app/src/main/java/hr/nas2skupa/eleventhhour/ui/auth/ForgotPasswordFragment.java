package hr.nas2skupa.eleventhhour.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;

/**
 * Created by nas2skupa on 26/04/16.
 */
@EFragment(R.layout.fragment_forgot_password)
public class ForgotPasswordFragment extends Fragment {
    private static final String TAG = "ForgotPasswordFragment";

    @ViewById
    ViewGroup layoutMain;

    @ViewById(R.id.layout_email)
    TextInputLayout layoutEmail;
    @ViewById(R.id.text_email)
    EditText textEmail;

    @ViewById(R.id.button_send_email)
    Button buttonSendEmail;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    public void setTextEmail(@ViewById(R.id.text_email) EditText textEmail) {
        if (getArguments() == null) return;
        String email = getArguments().getString("email");
        if (email != null) textEmail.setText(email);
    }

    @EditorAction(R.id.text_email)
    public boolean sendEmailKeyboard() {
        buttonSendEmail.callOnClick();
        return true;
    }

    @Click(R.id.button_send_email)
    public void sendEmail() {
        hideKeyboard();

        layoutEmail.setError(null);
        layoutEmail.setErrorEnabled(false);

        final String email = textEmail.getText().toString();
        final boolean isValidEmail = email.matches("^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$");

        if (!isValidEmail) {
            layoutEmail.setError(getString(R.string.msg_register_invalid_email));
            layoutEmail.setErrorEnabled(true);
            textEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");

                            Snackbar.make(layoutMain, R.string.msg_forgot_password_success, Snackbar.LENGTH_LONG).show();
                        }
                        else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Snackbar.make(layoutMain, R.string.msg_forgot_password_user_does_not_exist, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.msg_forgot_password_user_does_not_exist_action, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Bundle args = new Bundle();
                                            args.putString("email", email);
                                            RegisterFragment_ fragment = new RegisterFragment_();
                                            fragment.setArguments(args);
                                            getActivity().getSupportFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container, fragment, "RegisterFragment")
                                                    .addToBackStack("RegisterFragment")
                                                    .commit();
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(layoutMain, R.string.msg_forgot_password_fail, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(layoutMain.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
