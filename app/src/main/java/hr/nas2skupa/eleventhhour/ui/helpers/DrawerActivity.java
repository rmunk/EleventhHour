package hr.nas2skupa.eleventhhour.ui.helpers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.ui.MainActivity_;
import hr.nas2skupa.eleventhhour.ui.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.ui.auth.SignInActivity_;

/**
 * Created by nas2skupa on 05/11/2016.
 */

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    public DrawerLayout getDrawer() {
        return drawer;
    }

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        drawer = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer, null);

        final NavigationView navView = (NavigationView) drawer.findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) return;

                final TextView username = (TextView) navView.getHeaderView(0).findViewById(R.id.txt_drawer_username);
                final TextView email = (TextView) navView.getHeaderView(0).findViewById(R.id.txt_drawer_email);
                username.setText(currentUser.getDisplayName());
                email.setText(currentUser.getEmail());
            }
        });

        FrameLayout content = (FrameLayout) drawer.findViewById(R.id.content_main);
        getLayoutInflater().inflate(layoutResID, content, true);
        super.setContentView(drawer);
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

            case R.id.nav_sign_out:
                SignInActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).action(SignInActivity.ACTION_SIGN_OUT).start();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
