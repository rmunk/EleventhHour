package hr.nas2skupa.eleventhhour.ui;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DateRangeLimiter;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.ReminderService;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.DailyHours;
import hr.nas2skupa.eleventhhour.common.model.OpenHours;
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
    private OpenHours hours;

    public ServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTransitions();
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

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey)
                .child("hours")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            hours = dataSnapshot.getValue(OpenHours.class);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) adapter.cleanup();
    }

    private void showDatePicker(final Service service) {
        final LocalDateTime now = new LocalDateTime();

        final DatePickerDialog dpd = DatePickerDialog.newInstance(ServicesFragment.this);
        dpd.setTitle(service.name);
        dpd.setMinDate(Calendar.getInstance());
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.setDateRangeLimiter(new DateRangeLimiter() {
            @Override
            public int getMinYear() {
                return now.getYear();
            }

            @Override
            public int getMaxYear() {
                return 2100;
            }

            @NonNull
            @Override
            public Calendar getStartDate() {
                return Calendar.getInstance();
            }

            @NonNull
            @Override
            public Calendar getEndDate() {
                Calendar output = Calendar.getInstance();
                output.set(Calendar.YEAR, 2100);
                output.set(Calendar.DAY_OF_MONTH, 1);
                output.set(Calendar.MONTH, Calendar.JANUARY);
                return output;
            }

            @Override
            public boolean isOutOfRange(int year, int month, int day) {
                LocalDate date = new LocalDate(year, month + 1, day);
                DailyHours dailyHours = hours.getHours(date.getDayOfWeek());
                LocalDateTime closes = new LocalDateTime()
                        .withFields(date)
                        .withFields(dailyHours.getToDate())
                        .minusMinutes(selectedService.duration);
                return !dailyHours.isOpen() || !dailyHours.isValid() || (now.isAfter(closes));
            }

            @NonNull
            @Override
            public Calendar setToNearestDate(@NonNull Calendar calendar) {
                final int startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                while (isOutOfRange(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    // Don't loop forever if all days are closed
                    if (calendar.get(Calendar.DAY_OF_WEEK) == startDayOfWeek) {
                        Toast.makeText(getContext(), R.string.msg_services_provider_always_closed, Toast.LENGTH_LONG).show();
                        dpd.dismiss();
                        break;
                    }
                }
                return calendar;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }
        });

        dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        pickedDateTime = new GregorianCalendar(year, monthOfYear, dayOfMonth);

        LocalDate pickedDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);
        DailyHours pickedHours = this.hours.getHours(pickedDate.getDayOfWeek());
        LocalDateTime now = new LocalDateTime();
        now = now.withMinuteOfHour(15 - now.getMinuteOfHour() % 15).withSecondOfMinute(0).withMillisOfSecond(0);

        LocalDateTime opens = new LocalDateTime().withFields(pickedDate).withFields(pickedHours.getFromDate());
        LocalDateTime closes = new LocalDateTime().withFields(pickedDate).withFields(pickedHours.getToDate())
                .minusMinutes(selectedService.duration);

        TimePickerDialog tpd = TimePickerDialog.newInstance(ServicesFragment.this, true);
        tpd.setTitle(selectedService.name);
        tpd.setTimeInterval(1, 15);

        Timepoint minTime;
        Timepoint maxTime;
        if (opens.isAfter(now)) {
            minTime = new Timepoint(opens.getHourOfDay(), opens.getMinuteOfHour(), opens.getSecondOfMinute());
        } else {
            minTime = new Timepoint(now.getHourOfDay(), now.getMinuteOfHour(), now.getSecondOfMinute());
        }
        if (closes.isAfter(opens) && closes.isAfter(now)) {
            maxTime = new Timepoint(closes.getHourOfDay(), closes.getMinuteOfHour(), closes.getSecondOfMinute());
        } else maxTime = minTime;

        tpd.setMinTime(minTime);
        tpd.setMaxTime(maxTime);
        tpd.show(getActivity().getFragmentManager(), "TimePickerDialog");
    }


    @Override
    public void onTimeSet(TimePickerDialog timePickerDialog, int hourOfDay, int minute, int second) {
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
