package hr.nas2skupa.eleventhhour.panel;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.common.utils.StringUtils;

/**
 * Created by nas2skupa on 03/11/2016.
 */
@EFragment(R.layout.dialog_booking_details)
public class BookingDetailsDialog extends DialogFragment {
    @FragmentArg
    String bookingKey;

    @ViewById ProgressBar progressBar;
    @ViewById ViewGroup bookingContent;
    @ViewById TextView txtBookingDate;
    @ViewById TextView txtBookingService;
    @ViewById TextView txtBookingUser;
    @ViewById TextView txtBookingTime;
    @ViewById TextView txtBookingStatus;
    @ViewById TextView txtBookingPrice;
    @ViewById TextView txtBookingNote;
    @ViewById Button btnConfirmBooking;
    @ViewById Button btnRejectBooking;
    @ViewById Button btnCancelBooking;

    private View view;
    private DatabaseReference bookingReference;
    private ValueEventListener bookingListener;
    private Booking booking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingReference = FirebaseDatabase.getInstance().getReference()
                .child("providerAppointments")
                .child(MainActivity.providerKey)
                .child("data")
                .child(bookingKey);

        bookingListener = new BookingListener();

    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_booking_details, null);

        builder.setView(view);

        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(View.VISIBLE);
        bookingContent.setVisibility(View.INVISIBLE);
        bookingReference.addValueEventListener(bookingListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        progressBar.setVisibility(View.INVISIBLE);
        bookingContent.setVisibility(View.VISIBLE);
        bookingReference.removeEventListener(bookingListener);
    }

    @Click(R.id.btn_confirm_booking)
    void confirmBooking() {
        booking.status = BookingStatus.PROVIDER_ACCEPTED;
        saveBooking();
        dismiss();
    }

    @Click(R.id.btn_reject_booking)
    void rejectBooking() {
        booking.status = BookingStatus.PROVIDER_REJECTED;
        saveBooking();
        dismiss();
    }

    @Click(R.id.btn_cancel_booking)
    void cancelBooking() {
        booking.status = BookingStatus.PROVIDER_CANCELED;
        saveBooking();
        dismiss();
    }

    private void saveBooking() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = booking.key;
        Map<String, Object> bookingValues = booking.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/providerAppointments/%s/data/%s", booking.providerId, key), bookingValues);
        childUpdates.put(String.format("/userAppointments/%s/data/%s", booking.userId, key), bookingValues);
        reference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Snackbar.make(view, R.string.msg_booking_failed, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private class BookingListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            progressBar.setVisibility(View.INVISIBLE);
            bookingContent.setVisibility(View.VISIBLE);

            booking = dataSnapshot.getValue(Booking.class);

            if (booking != null) {
                booking.key = dataSnapshot.getKey();
                txtBookingDate.setText(booking.getDate());
                txtBookingService.setText(booking.serviceName);
                txtBookingUser.setText(booking.userName);
                txtBookingTime.setText(booking.getTime());
                txtBookingStatus.setText(StringUtils.printBookingStatus(getContext(), booking.getStatus()));
                txtBookingPrice.setText(booking.price);
                txtBookingNote.setText(booking.note);

                txtBookingUser.setVisibility(booking.userName.isEmpty() ? View.GONE : View.VISIBLE);
                txtBookingNote.setVisibility(booking.note.isEmpty() ? View.GONE : View.VISIBLE);

                if (booking.getStatus() == BookingStatus.PENDING) {
                    btnConfirmBooking.setVisibility(View.VISIBLE);
                    btnRejectBooking.setVisibility(View.VISIBLE);
                } else if (booking.getStatus() == BookingStatus.PROVIDER_ACCEPTED) {
                    btnCancelBooking.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
