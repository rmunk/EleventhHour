package hr.nas2skupa.eleventhhour.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.utils.StringUtils;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import hr.nas2skupa.eleventhhour.events.CancelBookingEvent;

/**
 * Created by nas2skupa on 03/11/2016.
 */
@EFragment(R.layout.dialog_booking_details)
public class BookingDetailsDialog extends DialogFragment implements OnMapReadyCallback {
    @Pref Preferences_ preferences;

    @FragmentArg String bookingKey;

    @ViewById TextView txtBookingDate;
    @ViewById TextView txtBookingService;
    @ViewById TextView txtBookingProvider;
    @ViewById TextView txtBookingTime;
    @ViewById TextView txtBookingStatus;
    @ViewById TextView txtBookingPrice;
    @ViewById TextView txtBookingNote;

    private View view;
    private DatabaseReference bookingReference;
    private ValueEventListener bookingListener;
    private GoogleMap map;
    private Marker marker;
    private Booking booking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingReference = FirebaseDatabase.getInstance().getReference()
                .child("userAppointments")
                .child(Utils.getMyUid())
                .child("data")
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
                .liteMode(true)
                .compassEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);

        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);

    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            if (getDialog() == null) return;

            booking = dataSnapshot.getValue(Booking.class);
            if (booking != null) {
                booking.key = dataSnapshot.getKey();
                txtBookingDate.setText(booking.getDate());
                txtBookingService.setText(booking.serviceName);
                txtBookingProvider.setText(booking.providerName);
                txtBookingTime.setText(booking.getTime());
                txtBookingStatus.setText(StringUtils.printBookingStatus(booking.getStatus()));
                txtBookingPrice.setText(booking.price);
                txtBookingNote.setText(booking.note);
                txtBookingNote.setVisibility(booking.note.isEmpty() ? View.GONE : View.VISIBLE);

                if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.PROVIDER_ACCEPTED)
                    ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE)
                            .setVisibility(View.VISIBLE);
                else ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setVisibility(View.GONE);


                FirebaseDatabase.getInstance().getReference()
                        .child("providers")
                        .child(preferences.country().get())
                        .child("data")
                        .child(booking.providerId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && getActivity() != null) {
                                    Provider provider = dataSnapshot.getValue(Provider.class);
                                    if (provider.location != null) {
                                        final LatLng target = provider.location.toLatLng();
                                        marker = map.addMarker(new MarkerOptions()
                                                .position(target)
                                                .title(booking.providerName));
                                        map.moveCamera(CameraUpdateFactory.newLatLng(target));
                                        marker.showInfoWindow();
                                    } else onCancelled(null);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getContext(), String.format(getString(R.string.booking_provider_error), booking.providerName), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
