package hr.nas2skupa.eleventhhour.panel;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
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
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Remove notifications token
                                    String token = FirebaseInstanceId.getInstance().getToken();
                                    if (token != null) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("app/notificationTokens/panel")
                                                .child(MainActivity.providerKey)
                                                .child(token).removeValue();
                                    }

                                    Intent intent = new Intent(DrawerActivity.this, SignInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(DrawerActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
        }
    }
}
