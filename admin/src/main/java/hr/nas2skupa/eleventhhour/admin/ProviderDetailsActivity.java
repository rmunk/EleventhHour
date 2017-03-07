package hr.nas2skupa.eleventhhour.admin;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;

import hr.nas2skupa.eleventhhour.model.Provider;

@EActivity(R.layout.activity_provider_details)
@OptionsMenu(R.menu.menu_provider_details)
public class ProviderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    @Extra String providerKey;

    @ViewById ViewGroup layoutMain;
    @ViewById CollapsingToolbarLayout toolbarLayout;
    @ViewById ViewGroup ratingHolder;
    @ViewById RatingBar ratingBar;

    @DimensionPixelOffsetRes int appBarHeight;

    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private SupportMapFragment mapFragment;
    private GoogleMap map;

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
    }

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setBuildingsEnabled(true);
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

    private class ProviderChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Provider newProvider = dataSnapshot.getValue(Provider.class);
            if (newProvider == null) return;

            final Provider provider = newProvider;
            provider.setKey(dataSnapshot.getKey());
            toolbarLayout.setTitle(provider.getName());
            ratingBar.setRating(provider.getRating());

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("geofire/providers")
                    .child(provider.getCategory())
                    .child(provider.getSubcategory());
            GeoFire geoFire = new GeoFire(ref);
            geoFire.getLocation(providerKey, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (map != null && location != null) {
                        LatLng target = new LatLng(location.latitude, location.longitude);
                        float[] hsl = new float[3];
                        ColorUtils.colorToHSL(ContextCompat.getColor(ProviderDetailsActivity.this, R.color.colorAccent), hsl);
                        map.addMarker(new MarkerOptions()
                                .position(target)
                                .icon(BitmapDescriptorFactory.defaultMarker(hsl[0]))
                                .title(provider.getName()));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(layoutMain, "Failed to load provider.", Snackbar.LENGTH_LONG).show();
        }
    }
}
