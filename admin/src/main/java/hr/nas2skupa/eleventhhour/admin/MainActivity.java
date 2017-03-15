package hr.nas2skupa.eleventhhour.admin;

import android.content.Intent;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;


@EActivity(R.layout.activity_main)
public class MainActivity extends DrawerActivity {
    public static final String ACTION_PROVIDERS = "hr.nas2skupa.eleventhhour.ACTION_PROVIDERS";
    public static final String ACTION_CATEGORIES = "hr.nas2skupa.eleventhhour.ACTION_CATEGORIES";
    public static final String ACTION_USERS = "hr.nas2skupa.eleventhhour.ACTION_USERS";

    @ViewById Toolbar toolbar;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setPage(intent.getAction());
    }

    void initToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        toolbar.setTitle(R.string.title_fragment_providers);
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
        if (action == null) action = ACTION_PROVIDERS;

        switch (action) {
            case ACTION_PROVIDERS:
                toolbar.setTitle(R.string.title_fragment_providers);
                if (getSupportFragmentManager().findFragmentByTag("HomeFragment") == null)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, ProvidersFragment_.builder().build(), "ProvidersFragment")
                            .commit();
                break;
            case ACTION_CATEGORIES:
                toolbar.setTitle(R.string.title_fragment_categories);
//                if (getSupportFragmentManager().findFragmentByTag("CalendarFragment") == null)
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, CategoriesFragment_.builder().build(), "CategoriesFragment")
//                            .commit();
                break;
            case ACTION_USERS:
                toolbar.setTitle(R.string.title_fragment_users);
//                if (getSupportFragmentManager().findFragmentByTag("FavoriteProvidersFragment") == null)
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, UsersFragment_.builder().build(), "UsersFragment")
//                            .commit();
                break;
            default:
                setPage(ACTION_PROVIDERS);
        }
    }
}
