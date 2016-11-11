package hr.nas2skupa.eleventhhour.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Category;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.ui.helpers.DrawerActivity;
import hr.nas2skupa.eleventhhour.ui.viewholders.ProviderViewHolder;

@EActivity(R.layout.activity_provider)
@OptionsMenu(R.menu.main)
public class ProviderActivity extends DrawerActivity {
    @Extra
    String categoryKey;
    @Extra
    String subcategoryKey;
    @Extra
    String providerKey;

    @ViewById(R.id.layout_main)
    ViewGroup layoutMain;
    @ViewById(R.id.app_bar)
    AppBarLayout appBar;

    private DatabaseReference categoryReference;
    private ValueEventListener categoryListener;
    private ProviderViewHolder providerViewHolder;
    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private boolean showDetails = false;

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getDrawer().setStatusBarBackgroundColor(Color.LTGRAY);
    }

    void setProviderView(@ViewById(R.id.item_provider) View providerInfo) {
        providerViewHolder = new ProviderViewHolder(providerInfo);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryReference = FirebaseDatabase.getInstance().getReference().child("categories").child(categoryKey);
        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(categoryKey)
                .child(subcategoryKey)
                .child(providerKey);

        if (savedInstanceState == null) {
            ServicesFragment fragment = ServicesFragment_.builder()
                    .categoryKey(categoryKey)
                    .subcategoryKey(subcategoryKey)
                    .providerKey(providerKey)
                    .build();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "ServicesFragment")
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

//        EventBus.getDefault().register(this);

        categoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category == null) return;

                int color;
                try {
                    color = Color.parseColor(category.getColor());
                } catch (Exception e) {
                    color = Color.LTGRAY;
                }
                getDrawer().setStatusBarBackgroundColor(color);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        categoryReference.addValueEventListener(categoryListener);

        providerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Provider provider = dataSnapshot.getValue(Provider.class);
                if (provider == null) return;

                providerViewHolder.bindToProvider(provider);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(layoutMain, "Failed to load provider.",
                        Snackbar.LENGTH_LONG).show();
            }
        };
        providerReference.addValueEventListener(providerListener);
    }

    @Override
    public void onStop() {
        super.onStop();

//        EventBus.getDefault().unregister(this);

        if (categoryListener != null) categoryReference.removeEventListener(categoryListener);
        if (providerListener != null) providerReference.removeEventListener(providerListener);
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @Click(R.id.item_provider)
    public void showProviderDetails(CardView providerCard) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Transition transition = new AutoTransition();
            transition.setDuration(200);
            TransitionManager.beginDelayedTransition(layoutMain, transition);
        }

        showDetails = !showDetails;
        providerViewHolder.showDetails(showDetails);
    }
}
