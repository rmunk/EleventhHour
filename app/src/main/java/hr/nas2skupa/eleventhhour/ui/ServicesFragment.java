package hr.nas2skupa.eleventhhour.ui;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.ReminderService;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.model.Service;
import hr.nas2skupa.eleventhhour.common.ui.MakeBookingDialog;
import hr.nas2skupa.eleventhhour.common.ui.MakeBookingDialog_;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import hr.nas2skupa.eleventhhour.ui.viewholders.ServiceViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_recycler_view)
public class ServicesFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        MakeBookingDialog.BookingDialogListener {

    @Pref Preferences_ preferences;

    @FragmentArg String providerKey;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private String serviceKey;
    private Service selectedService;
    private GregorianCalendar pickedDateTime;
    private boolean undo = false;
    private FirebaseRecyclerAdapter<Service, ServiceViewHolder> adapter;

    public ServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTransitions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) adapter.cleanup();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        Query query = FirebaseDatabase.getInstance().getReference()
                .child("providerServices")
                .child(providerKey)
                .child("data")
                .orderByChild("name");
        adapter = new FirebaseRecyclerAdapter<Service, ServiceViewHolder>(
                Service.class,
                R.layout.item_service,
                ServiceViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final ServiceViewHolder viewHolder, final Service model, final int position) {
                viewHolder.bindToService(model);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final DatabaseReference categoryRef = getRef(position);
                        serviceKey = categoryRef.getKey();
                        selectedService = model;

                        showDatePicker(model);
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void showDatePicker(Service service) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                ServicesFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setTitle(service.name);
        dpd.setMinDate(now);
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        pickedDateTime = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                ServicesFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true);
        tpd.setTitle(selectedService.name);
        tpd.setTimeInterval(1, 15);
        if (now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) == monthOfYear && now.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
            tpd.setMinTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
        tpd.show(getActivity().getFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        pickedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        pickedDateTime.set(Calendar.MINUTE, minute);
        pickedDateTime.set(Calendar.SECOND, second);

        final Calendar to = (Calendar) pickedDateTime.clone();
        to.add(Calendar.MINUTE, selectedService.duration);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment last = getFragmentManager().findFragmentByTag("dialog");
        if (last != null) transaction.remove(last);
        transaction.addToBackStack(null);
        transaction.commit();

        FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Provider provider = dataSnapshot.getValue(Provider.class);
                        if (provider == null) onCancelled(null);

                        MakeBookingDialog makeBookingDialog = MakeBookingDialog_.builder()
                                .userKey(Utils.getMyUid())
                                .providerKey(providerKey)
                                .serviceKey(serviceKey)
                                .from(pickedDateTime)
                                .to(to)
                                .userName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
                                .providerName(provider.name)
                                .serviceName(selectedService.name)
                                .price(selectedService.price)
                                .build();
                        makeBookingDialog.setBookingDialogListener(ServicesFragment.this);
                        makeBookingDialog.show(getFragmentManager(), "MakeBookingDialog");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (getView() != null)
                            Snackbar.make(getView(), R.string.msg_booking_failed, Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    @UiThread(delay = 5500)
    public void sendBooking(final Booking booking) {
        if (undo) return;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        booking.key = reference.child(String.format("/providerAppointments/%s/data", booking.providerId)).push().getKey();
        Map<String, Object> bookingValues = booking.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/providerAppointments/%s/data/%s", booking.providerId, booking.key), bookingValues);
        childUpdates.put(String.format("/userAppointments/%s/data/%s", booking.userId, booking.key), bookingValues);
        reference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    setReminder(booking);
                } else {
                    Snackbar.make(getView(), R.string.msg_booking_failed, Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    private void setReminder(Booking booking) {
        final int id = booking.key.hashCode();
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getContext(), ReminderService.class);
        alarmIntent.putExtra("bookingKey", booking.key);
        alarmIntent.putExtra("providerKey", booking.providerId);
        alarmIntent.putExtra("userKey", booking.userId);
        PendingIntent pendingAlarmIntent = PendingIntent.getService(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(booking.from);
        calendar.add(Calendar.HOUR_OF_DAY, -1);

        if (calendar.after(Calendar.getInstance())) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingAlarmIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTransitions() {
        setAllowEnterTransitionOverlap(false);
        setAllowReturnTransitionOverlap(false);

        setEnterTransition(new Fade());
        setReenterTransition(new Fade());
        setExitTransition(new Slide(Gravity.TOP));
        setReturnTransition(new Slide(Gravity.TOP));
    }

    @Override
    public void onBookingConfirmed(Booking booking) {
        undo = false;
        Snackbar.make(getView(), R.string.msg_booking_sent, 5000)
                .setAction(R.string.action_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undo = true;
                    }
                })
                .show();
        sendBooking(booking);
    }

    @Override
    public void onBookingDismissed() {

    }
}
