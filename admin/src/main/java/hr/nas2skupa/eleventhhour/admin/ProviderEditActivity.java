package hr.nas2skupa.eleventhhour.admin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.common.ui.provider.ProviderFragment;
import hr.nas2skupa.eleventhhour.common.ui.provider.ProviderFragment_;

@EActivity(R.layout.activity_provider_edit)
public class ProviderEditActivity extends AppCompatActivity {
    @Extra String providerKey;

    private ProviderFragment providerFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerFragment = ProviderFragment_.builder()
                .providerKey(providerKey)
                .editable(true)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, providerFragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    public void cancel(View view) {
        onBackPressed();
    }

    public void save(final View view) {
        providerFragment.saveProvider(new ProviderFragment.SaveProviderListener() {
            @Override
            public void onProviderSavedListener(String key, boolean saved) {
                if (saved) {
                    ProviderDetailsActivity_.intent(ProviderEditActivity.this).providerKey(key).start();
                } else {
                    Snackbar.make(view, R.string.msg_provider_save_failed, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
