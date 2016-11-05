package hr.nas2skupa.eleventhhour.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.CancelBookingEvent;
import hr.nas2skupa.eleventhhour.model.Booking;
import hr.nas2skupa.eleventhhour.utils.StringUtils;
import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * Created by nas2skupa on 03/11/2016.
 */
@EFragment(R.layout.dialog_booking_details)
public class BookingDetailsDialog extends DialogFragment implements OnMapReadyCallback {
    @FragmentArg
    String bookingKey;

    @ViewById(R.id.txt_booking_service)
    TextView txtBookingService;
    @ViewById(R.id.txt_booking_provider)
    TextView txtBookingProvider;
    @ViewById(R.id.txt_booking_time)
    TextView txtBookingTime;
    @ViewById(R.id.txt_booking_status)
    TextView txtBookingStatus;
    @ViewById(R.id.txt_booking_price)
    TextView txtBookingPrice;
    @ViewById(R.id.txt_booking_note)
    TextView txtBookingNote;

    private View view;
    private DatabaseReference bookingReference;
    private ValueEventListener bookingListener;
    private GoogleMap map;
    private Marker marker;
    private GeoFire geoFire;
    private Booking booking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("bookings")
                .child(bookingKey);

        bookingListener = new BookingListener();

    }

    @Override
    public void onStart() {
        super.onStart();

        bookingReference.addValueEventListener(bookingListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        bookingReference.removeEventListener(bookingListener);
    }

    @AfterViews
    public void setMap() {
        LatLng target = new LatLng(45.8352055, 15.818705);
        int zoom = 16;
        GoogleMapOptions options = new GoogleMapOptions()
                .camera(CameraPosition.fromLatLngZoom(target, zoom))
//                .liteMode(true)
                .compassEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);

        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_booking_details, null);

        builder.setView(view);
        builder.setPositiveButton(R.string.cancel_booking, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new CancelBookingEvent(booking));
                dismiss();
            }
        });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setBuildingsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            map.setMyLocationEnabled(true);

    }

    private class BookingListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            booking = dataSnapshot.getValue(Booking.class);

            if (booking != null) {
                booking.setKey(dataSnapshot.getKey());
                txtBookingService.setText(booking.getServiceName());
                txtBookingProvider.setText(booking.getProviderName());
                txtBookingTime.setText(booking.getTime());
                txtBookingStatus.setText(StringUtils.printBookingStatus(getContext(), booking.getStatus()));
                txtBookingPrice.setText(booking.getPrice());
                txtBookingNote.setText(booking.getNote());

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("geofire/providers");
                geoFire = new GeoFire(ref);
                geoFire.getLocation(booking.getProviderId(), new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (location != null) {
                            LatLng target = new LatLng(location.latitude, location.longitude);
                            marker = map.addMarker(new MarkerOptions()
                                    .position(target)
                                    .title(booking.getProviderName())
                                    .snippet(getString(R.string.tap_for_directions)));
                            map.moveCamera(CameraUpdateFactory.newLatLng(target));
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
