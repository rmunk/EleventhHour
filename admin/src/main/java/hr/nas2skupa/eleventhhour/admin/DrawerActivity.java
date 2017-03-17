package hr.nas2skupa.eleventhhour.admin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import hr.nas2skupa.eleventhhour.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.ui.helpers.AbstractDrawerActivity;

/**
 * Created by nas2skupa on 05/11/2016.
 */

public class DrawerActivity extends AbstractDrawerActivity {


    @Override
    public void onItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_providers:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_PROVIDERS).start();
                break;
            case R.id.nav_categories:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_CATEGORIES).start();
                break;
            case R.id.nav_users:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_USERS).start();
                break;

            case R.id.nav_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(DrawerActivity.this, SignInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(DrawerActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG);
                                }
                            }
                        });
                break;
        }
    }
}
