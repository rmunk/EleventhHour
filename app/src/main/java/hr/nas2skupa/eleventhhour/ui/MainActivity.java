package hr.nas2skupa.eleventhhour.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.utils.Utils;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends DrawerActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ACTION_HOME = "hr.nas2skupa.eleventhhour.ACTION_HOME";
    public static final String ACTION_CALENDAR = "hr.nas2skupa.eleventhhour.ACTION_CALENDAR";
    public static final String ACTION_FAVORITES = "hr.nas2skupa.eleventhhour.ACTION_FAVORITES";
    public static final String ACTION_TOP = "hr.nas2skupa.eleventhhour.ACTION_TOP";
    public static final String ACTION_PROFILE = "hr.nas2skupa.eleventhhour.ACTION_PROFILE";
    public static final String ACTION_HELP = "hr.nas2skupa.eleventhhour.ACTION_HELP";

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setPage(intent.getAction());
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (getDrawer().isDrawerOpen(GravityCompat.START))
            getDrawer().closeDrawer(GravityCompat.START);
        else if (getSupportFragmentManager().findFragmentByTag("HomeFragment") == null)
            setPage(ACTION_HOME);
        else super.onBackPressed();
    }

    void initToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        toolbar.setTitle(R.string.title_fragment_home);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        getDrawer().addDrawerListener(toggle);
        toggle.syncState();
    }

    @AfterViews
    public void afterViews() {
        setPage(getIntent().getAction());
        handleIntent(getIntent());
    }

    private void setPage(@NonNull String action) {
        switch (action) {
            case ACTION_HOME:
                toolbar.setTitle(R.string.title_fragment_home);
                if (getSupportFragmentManager().findFragmentByTag("HomeFragment") == null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment_.builder().build(), "HomeFragment")
                            .commit();
                break;
            case ACTION_CALENDAR:
                toolbar.setTitle(R.string.title_fragment_calendar);
                if (getSupportFragmentManager().findFragmentByTag("CalendarFragment") == null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, CalendarFragment_.builder().build(), "CalendarFragment")
                            .commit();
                break;
            case ACTION_FAVORITES:
                toolbar.setTitle(R.string.title_fragment_favorites);
                if (getSupportFragmentManager().findFragmentByTag("FavoriteProvidersFragment") == null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, FavoriteProvidersFragment_.builder().build(), "FavoriteProvidersFragment")
                            .commit();
                break;
            case ACTION_TOP:
                toolbar.setTitle(R.string.title_fragment_top);
                if (getSupportFragmentManager().findFragmentByTag("TopProvidersFragment") == null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, TopProvidersFragment_.builder().build(), "TopProvidersFragment")
                            .commit();
                break;

            case ACTION_PROFILE:
                UserDetailsActivity_.intent(this).extra("userKey", Utils.getMyUid()).start();
                break;
            case ACTION_HELP:
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_page)));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "You can't access help without web browser!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                setPage(ACTION_HOME);
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        if (intent.hasExtra("bookingKey") && intent.hasExtra("userKey")) {

            String bookingKey = intent.getStringExtra("bookingKey");
            String userKey = intent.getStringExtra("userKey");

            if (!userKey.equals(Utils.getMyUid())) return;

            BookingDetailsDialog_.builder()
                    .bookingKey(bookingKey)
                    .build()
                    .show(getSupportFragmentManager(), "BookingDetailsDialog");
        }
    }
}
