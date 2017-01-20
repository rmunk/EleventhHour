package hr.nas2skupa.eleventhhour.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;

@EActivity(R.layout.activity_map)
public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_LOCATION = 1234;

    @Extra
    String categoryKey;
    @Extra
    String subcategoryKey;

    @ViewById(R.id.bottom_sheet)
    ViewGroup bottomSheet;
    @ViewById(R.id.fab)
    FloatingActionButton fab;

    private GoogleMap map;
    private GeoQueryEventListener geoQueryEventListener;
    private GeoQuery geoQuery;
    private LatLng zagreb;
    private HashMap<String, Marker> markers = new HashMap<>();

    private BottomSheetBehavior<ViewGroup> behavior;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private String providerKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        zagreb = new LatLng(45.813316, 15.982176);

        DatabaseReference geoFireReference = FirebaseDatabase.getInstance().getReference()
                .child("geofire/providers/")
                .child(categoryKey)
                .child(subcategoryKey);
        GeoFire geoFire = new GeoFire(geoFireReference);
        geoQueryEventListener = new GeoQueryEventListener();
        geoQuery = geoFire.queryAtLocation(new GeoLocation(zagreb.latitude, zagreb.longitude), 2);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @AfterViews
    public void init() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) fab.show();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset < 0) {
                    fab.setScaleX(1 + slideOffset);
                    fab.setScaleY(1 + slideOffset);
                } else {
                    fab.setScaleX(1);
                    fab.setScaleY(1);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (map != null) map.clear();
        geoQuery.addGeoQueryEventListener(geoQueryEventListener);

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (geoQueryEventListener != null)
            geoQuery.removeGeoQueryEventListener(geoQueryEventListener);

        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setBuildingsEnabled(true);
        map.setOnCameraIdleListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(zagreb, 13));

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                providerKey = (String) marker.getTag();

                String distance = "";
                if (lastLocation != null) {
                    Location location = new Location("marker");
                    location.setLatitude(marker.getPosition().latitude);
                    location.setLongitude(marker.getPosition().longitude);
                    float distanceTo = lastLocation.distanceTo(location);
                    distance = distanceTo < 1000
                            ? String.format(Locale.getDefault(), "%.0f m", distanceTo)
                            : String.format(Locale.getDefault(), "%.1f km", distanceTo / 1000);
                }
                if (getSupportFragmentManager().findFragmentByTag(providerKey) == null) {
                    ProviderInfoFragment fragment = ProviderInfoFragment_.builder()
                            .providerKey(providerKey)
                            .distance(distance)
                            .build();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, providerKey).commit();
                }
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                return true;
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else map.setMyLocationEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);
                    if (googleApiClient.isConnected())
                        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                }
                break;
        }
    }

    private float getMapHeight() {
        VisibleRegion region = map.getProjection().getVisibleRegion();

        Location center = new Location("center");
        center.setLatitude(region.latLngBounds.getCenter().latitude);
        center.setLongitude(region.latLngBounds.getCenter().longitude);

        Location middleTopLocation = new Location(center);
        middleTopLocation.setLatitude(region.latLngBounds.northeast.latitude);

        return center.distanceTo(middleTopLocation);
    }

    @Override
    public void onCameraIdle() {
        LatLng target = map.getCameraPosition().target;
        geoQuery.setLocation(new GeoLocation(target.latitude, target.longitude), getMapHeight() / 1000);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Click(R.id.fab)
    public void showProvider() {
        ProviderActivity_.intent(this)
                .providerKey(providerKey)
                .start();
    }

    private class GeoQueryEventListener implements com.firebase.geofire.GeoQueryEventListener {

        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            LatLng target = new LatLng(location.latitude, location.longitude);
            Marker marker = map.addMarker(new MarkerOptions().position(target));
            marker.setTag(key);
            markers.put(key, marker);
        }

        @Override
        public void onKeyExited(String key) {
            Marker marker = markers.remove(key);
            if (marker != null) marker.remove();
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            LatLng target = new LatLng(location.latitude, location.longitude);
            Marker marker = markers.get(key);
            if (marker != null) marker.setPosition(target);
        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    }
}