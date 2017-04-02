package hr.nas2skupa.eleventhhour.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.auth.SignInActivity;

/**
 * Created by nas2skupa on 05/11/2016.
 */

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView navView;
    private FirebaseAuth.AuthStateListener authStateListener;

    public DrawerLayout getDrawer() {
        return drawer;
    }

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        drawer = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer, null);

        navView = (NavigationView) drawer.findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        FrameLayout content = (FrameLayout) drawer.findViewById(R.id.content_main);
        getLayoutInflater().inflate(layoutResID, content, true);
        super.setContentView(drawer);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) return;

                final TextView username = (TextView) navView.getHeaderView(0).findViewById(R.id.txt_drawer_username);
                final TextView email = (TextView) navView.getHeaderView(0).findViewById(R.id.txt_drawer_email);
                username.setText(currentUser.getDisplayName());
                email.setText(currentUser.getEmail());
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_HOME).start();
                break;
            case R.id.nav_calendar:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_CALENDAR).start();
                break;
            case R.id.nav_favorites:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_FAVORITES).start();
                break;
            case R.id.nav_top:
                MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).action(MainActivity.ACTION_TOP).start();
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

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
