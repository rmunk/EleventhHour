package hr.nas2skupa.eleventhhour.admin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.model.Provider;

@EActivity(R.layout.activity_provider_edit)
public class ProviderEditActivity extends AppCompatActivity {
    @Extra String providerKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProviderFragment providerFragment = ProviderFragment_.builder()
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

    public void save(View view) {
    }
}
