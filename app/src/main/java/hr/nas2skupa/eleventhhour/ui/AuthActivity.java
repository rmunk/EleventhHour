package hr.nas2skupa.eleventhhour.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.auth.SignInActivity;

/**
 * Created by nas2skupa on 15/03/2017.
 */

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase.getInstance().getReference()
                .child("notificationTokens")
                .child("client")
                .child(user.getUid())
                .child(token)
                .setValue(true);

        MainActivity_.intent(AuthActivity.this)
                .action(MainActivity.ACTION_HOME)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
    }

    void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(AuthActivity.this, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AuthActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG);
                            finish();
                        }
                    }
                });
    }
}
