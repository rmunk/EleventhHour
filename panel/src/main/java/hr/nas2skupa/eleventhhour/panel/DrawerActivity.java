package hr.nas2skupa.eleventhhour.panel;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import hr.nas2skupa.eleventhhour.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.common.ui.helpers.AbstractDrawerActivity;

/**
 * Created by nas2skupa on 05/11/2016.
 */

public class DrawerActivity extends AbstractDrawerActivity {


    @Override
    public void onItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_planer:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_PLANER).start();
                break;
            case R.id.nav_profile:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_PROFILE).start();
                break;
            case R.id.nav_help:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_HELP).start();
                break;
            case R.id.nav_sign_out:
                final String token = FirebaseInstanceId.getInstance().getToken();

                // Remove notifications token
                if (token != null) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("app/notificationTokens/panel")
                            .child(MainActivity.providerKey)
                            .child(token)
                            .removeValue();
                }

                AuthUI.getInstance()
                        .signOut(this)
                        .continueWith(new Continuation<Void, Void>() {
                            @Override
                            public Void then(@NonNull Task<Void> task) throws Exception {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(DrawerActivity.this, SignInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Restore notifications token
                                    if (token != null) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("app/notificationTokens/panel")
                                                .child(MainActivity.providerKey)
                                                .child(token)
                                                .setValue(true);
                                    }
                                    Toast.makeText(DrawerActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG).show();
                                }
                                return null;
                            }
                        });
                break;
        }
    }
}
