package hr.nas2skupa.eleventhhour.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;

@EActivity(R.layout.activity_provider)
public class ProviderActivity extends AppCompatActivity {
    @Extra
    String providerKey;

    @ViewById(R.id.main_layout)
    ViewGroup mainLayout;
    @ViewById(R.id.app_bar)
    AppBarLayout appBar;

    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
