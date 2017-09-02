package hr.nas2skupa.eleventhhour.panel;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;

import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.ui.provider.ProviderFragment;
import hr.nas2skupa.eleventhhour.common.ui.provider.ProviderFragment_;
import hr.nas2skupa.eleventhhour.common.ui.provider.ServiceDialog_;
import hr.nas2skupa.eleventhhour.common.ui.provider.ServicesFragment;
import hr.nas2skupa.eleventhhour.common.ui.provider.ServicesFragment_;

@EActivity(R.layout.activity_provider_details)
@OptionsMenu(R.menu.menu_provider_details)
public class ProviderDetailsActivity extends DrawerActivity implements OnMapReadyCallback {
    @Extra String providerKey;

    @ViewById ViewGroup layoutMain;
    @ViewById AppBarLayout appBar;
    @ViewById CollapsingToolbarLayout toolbarLayout;
    @ViewById NestedScrollView nestedScroll;
    @ViewById ViewGroup ratingHolder;
    @ViewById RatingBar ratingBar;
    @ViewById FloatingActionButton fabServices;
    @ViewById FloatingActionButton fabAddService;

    @DimensionPixelOffsetRes int appBarHeight;

    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private GoogleMap map;
    private Provider provider = new Provider();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(providerKey);
        providerListener = new ProviderChangedListener();

        ProviderFragment providerFragment = ProviderFragment_.builder()
                .providerKey(providerKey)
                .editable(false)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, providerFragment)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();

        providerReference.addValueEventListener(providerListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        providerReference.removeEventListener(providerListener);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            appBar.setExpanded(true);
            nestedScroll.setNestedScrollingEnabled(true);
            fabAddService.hide();
        }
        super.onBackPressed();
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @OptionsItem(R.id.action_edit)
    void editProvider() {
        ProviderEditActivity_.intent(this).providerKey(providerKey).start();
    }

    @OptionsItem(R.id.action_delete)
    void deleteProvider() {
        new AlertDialog.Builder(this)
                .setTitle(String.format(getString(R.string.action_delete_title), provider.name))
                .setMessage(String.format(getString(R.string.action_delete_message), provider.name))
                .setPositiveButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Remove provider from subcategories
                        providerReference.removeValue();
                        onBackPressed();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create()
                .show();
    }

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    void setAppBar(@ViewById(R.id.app_bar) AppBarLayout appBar) {
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                ratingHolder.setVisibility(verticalOffset < 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    @AfterViews
    void initMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setBuildingsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Don't open Google maps
            }
        });
        int padding = toolbarLayout.getWidth() / 2 - 60;
        map.setPadding(padding, 0, padding, 0);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("Map", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Map", "Can't find style. Error: ", e);
        }
    }

    @Click(R.id.fab_services)
    void editServices() {
        appBar.setExpanded(false);
        nestedScroll.setNestedScrollingEnabled(false);
        ServicesFragment servicesFragment = ServicesFragment_.builder()
                .providerKey(providerKey)
                .build();
        servicesFragment.setOnServiceClickListener(new ServicesFragment.OnServiceClickListener() {
            @Override
            public void onServiceClick(View view, String providerKey, String serviceKey) {
                ServiceDialog_.builder()
                        .providerKey(providerKey)
                        .serviceKey(serviceKey)
                        .build()
                        .show(getSupportFragmentManager(), "ServiceDialog");
            }
        });
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("ServicesFragment")
                .replace(R.id.fragment_container, servicesFragment)
                .commit();
        fabAddService.show();
    }

    @Click(resName = "fab_add_service")
    void addService() {
        ServiceDialog_.builder()
                .providerKey(providerKey)
                .serviceKey(null)
                .build()
                .show(getSupportFragmentManager(), "ServiceDialog");
    }

    private class ProviderChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Provider newProvider = dataSnapshot.getValue(Provider.class);
            if (newProvider == null) return;

            provider = newProvider;
            provider.key = dataSnapshot.getKey();
            toolbarLayout.setTitle(provider.name);
            ratingBar.setRating(provider.rating);

            if (map != null && provider.location != null) {
                LatLng target = new LatLng(provider.location.latitude, provider.location.longitude);
                float[] hsl = new float[3];
                ColorUtils.colorToHSL(ContextCompat.getColor(ProviderDetailsActivity.this, R.color.colorAccent), hsl);
                map.addMarker(new MarkerOptions()
                        .position(target)
                        .icon(BitmapDescriptorFactory.defaultMarker(hsl[0])));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
