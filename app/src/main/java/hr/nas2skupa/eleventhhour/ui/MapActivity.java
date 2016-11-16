package hr.nas2skupa.eleventhhour.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
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

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.R;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {
    private static final int REQUEST_LOCATION = 1234;

    private GoogleMap map;
    private GeoFire geoFire;
    private GeoQueryEventListener geoQueryEventListener;
    private GeoQuery geoQuery;
    private LatLng zagreb;
    private HashMap<String, Marker> markers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        zagreb = new LatLng(45.813316, 15.982176);

        DatabaseReference geoFireReference = FirebaseDatabase.getInstance().getReference().child("geofire/providers/health/polyclinic");
        geoFire = new GeoFire(geoFireReference);
        geoQueryEventListener = new GeoQueryEventListener();
        geoQuery = geoFire.queryAtLocation(new GeoLocation(zagreb.latitude, zagreb.longitude), 2);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (map != null) map.clear();
        geoQuery.addGeoQueryEventListener(geoQueryEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (geoQueryEventListener != null)
            geoQuery.removeGeoQueryEventListener(geoQueryEventListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setBuildingsEnabled(true);
        map.setOnCameraIdleListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(zagreb, 14));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else map.setMyLocationEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    map.setMyLocationEnabled(true);
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

    private class GeoQueryEventListener implements com.firebase.geofire.GeoQueryEventListener {

        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            LatLng target = new LatLng(location.latitude, location.longitude);
            markers.put(key, map.addMarker(new MarkerOptions().position(target)));
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
