package hr.nas2skupa.eleventhhour.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.ui.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.ui.auth.SignInActivity_;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String HOME = "ch.photrack.discharge.HOME";
    public static final String ORGANIZER = "ch.photrack.discharge.ORGANIZER";
    public static final String MESSAGES = "ch.photrack.discharge.MESSAGES";
    public static final String FAVOURITES = "ch.photrack.discharge.FAVOURITES";
    public static final String SALE = "ch.photrack.discharge.SALE";

    @ViewById(R.id.drawer)
    DrawerLayout drawer;

    void initToolbar(@ViewById(R.id.toolbar) Toolbar toolbar,
                     @ViewById(R.id.drawer) DrawerLayout drawer,
                     @ViewById(R.id.nav_view) NavigationView navigationView) {
        toolbar.setTitle(R.string.title_fragment_home);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_sign_out) {
            SignInActivity_.intent(this)
                    .action(SignInActivity.ACTION_SIGN_OUT)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                    .start();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
