package hr.nas2skupa.eleventhhour.panel;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.auth.SignInActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends DrawerActivity {

    public static final String ACTION_PLANER = "hr.nas2skupa.eleventhhour.panel.ACTION_PLANER";
    public static final String ACTION_PROFILE = "hr.nas2skupa.eleventhhour.panel.ACTION_PROFILE";
    public static final String ACTION_HELP = "hr.nas2skupa.eleventhhour.panel.ACTION_HELP";

    @Extra
    public static String providerKey;

    @ViewById Toolbar toolbar;

    private MyAuthStateListener authStateListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        authStateListener = new MyAuthStateListener();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setPage(intent.getAction());
    }

    void initToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        toolbar.setTitle(R.string.title_fragment_planer);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        getDrawer().addDrawerListener(toggle);
        toggle.syncState();
    }

    @AfterViews
    public void afterViews() {
        setPage(getIntent().getAction());
    }

    private void setPage(String action) {
        if (action == null) action = ACTION_PLANER;

        switch (action) {
            case ACTION_PLANER:
                toolbar.setTitle(R.string.title_fragment_planer);
                if (getSupportFragmentManager().findFragmentByTag("PlanerFragment") == null) {
                    PlanerFragment planerFragment = PlanerFragment_.builder().providerKey(providerKey).build();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, planerFragment, "PlanerFragment")
                            .commit();
                }
                break;
            case ACTION_PROFILE:
                ProviderDetailsActivity_.intent(this).providerKey(providerKey).start();
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
                setPage(ACTION_PLANER);
        }
    }

    private class MyAuthStateListener implements FirebaseAuth.AuthStateListener {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null || providerKey == null) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
            }
        }
    }
}
