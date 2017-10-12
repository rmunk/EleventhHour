package hr.nas2skupa.eleventhhour.panel;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.TextView;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.model.Service;
import hr.nas2skupa.eleventhhour.common.ui.MakeBookingDialog;
import hr.nas2skupa.eleventhhour.common.ui.MakeBookingDialog_;
import hr.nas2skupa.eleventhhour.common.ui.helpers.DelayedProgressDialog;
import hr.nas2skupa.eleventhhour.common.utils.ColorGenerator;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_planer)
@OptionsMenu(R.menu.menu_planer)
public class PlanerFragment extends Fragment
        implements MonthLoader.MonthChangeListener,
        WeekView.EventClickListener,
        ChildEventListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        MakeBookingDialog.BookingDialogListener {

    private static final int START_HOUR = 8;
    private static final long PROGRESS_DELAY = 500l;

    @Pref Preferences_ preferences;

    @FragmentArg String providerKey;

    private List<MyWeekViewEvent> events = new ArrayList<>();

    private Map<String, Query> bookingsQueryList = new HashMap<>();

    private ProgressDialog progressDialog;
    private boolean showCancelled = false;

    @ViewById WeekView weekView;

    private GregorianCalendar pickedDateTime;
    private Service selectedService;

    public PlanerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        if (weekView != null) weekView.notifyDatasetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

        for (Query query : bookingsQueryList.values()) {
            query.removeEventListener(this);
        }
        bookingsQueryList.clear();
    }

    void initWeekView(@ViewById(R.id.week_view) final WeekView weekView) {
        weekView.setOnEventClickListener(this);
        weekView.setMonthChangeListener(this);
        weekView.goToHour(START_HOUR);
        weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
            @Override
            public void onEmptyViewClicked(Calendar time) {
                Log.d(getTag(), "onEmptyViewClicked() called with: time = [" + time.getTime().toString() + "]");
            }
        });
        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                switch (weekView.getNumberOfVisibleDays()) {
                    case 1:
                        return new SimpleDateFormat("EEEE, ", Locale.getDefault()).format(date.getTime())
                                + DateFormat.getLongDateFormat(getContext()).format(date.getTime());
                    case 3:
                        return new SimpleDateFormat("EEE M/d", Locale.getDefault()).format(date.getTime()).toUpperCase();
                    default:
                        return new SimpleDateFormat("EEE M/d", Locale.getDefault()).format(date.getTime()).toUpperCase().charAt(0)
                                + new SimpleDateFormat(" M/d", Locale.getDefault()).format(date.getTime());
                }


/*
                DateFormat.getBestDateTimePattern(Locale.getDefault(), "")
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (weekView.getNumberOfVisibleDays() > 3)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
 */
            }

            @Override
            public String interpretTime(int hour) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, 0);

                try {
                    SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("hh a", Locale.getDefault());
                    return sdf.format(calendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        });
    }

    @OptionsItem(R.id.menu_show_one_day)
    void showOneDay(MenuItem item) {
        item.setChecked(true);

        Calendar firstVisibleDay = weekView.getFirstVisibleDay();
        double firstVisibleHour = weekView.getFirstVisibleHour();
        weekView.setNumberOfVisibleDays(1);
        weekView.goToDate(firstVisibleDay);
        weekView.goToHour(firstVisibleHour);

        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    @OptionsItem(R.id.menu_show_three_days)
    void showThreeDays(MenuItem item) {
        item.setChecked(true);

        Calendar firstVisibleDay = weekView.getFirstVisibleDay();
        double firstVisibleHour = weekView.getFirstVisibleHour();
        weekView.setNumberOfVisibleDays(3);
        weekView.goToDate(firstVisibleDay);
        weekView.goToHour(firstVisibleHour);

        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    @OptionsItem(R.id.menu_show_week)
    void menuShowWeek(MenuItem item) {
        item.setChecked(true);

        Calendar firstVisibleDay = weekView.getFirstVisibleDay();
        double firstVisibleHour = weekView.getFirstVisibleHour();
        weekView.setNumberOfVisibleDays(7);
        weekView.goToDate(firstVisibleDay);
        weekView.goToHour(firstVisibleHour);

        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
    }

    @OptionsItem(R.id.menu_today)
    void goToToday() {
        weekView.goToToday();
        weekView.goToHour(START_HOUR);
    }

    @OptionsItem(R.id.menu_show_cancelled)
    void toggleCanceledBookings(MenuItem item) {
        showCancelled = !showCancelled;
        item.setChecked(showCancelled);

        events.clear();
        for (Query query : bookingsQueryList.values()) {
            query.removeEventListener(this);
            query.addChildEventListener(this);
        }
    }

    @Override
    public List<? extends MyWeekViewEvent> onMonthChange(int newYear, final int newMonth) {
        String key = newYear + "-" + newMonth;
        if (!bookingsQueryList.containsKey(key)) {
            GregorianCalendar calendar = new GregorianCalendar(newYear, newMonth - 1, 0);
            long from = calendar.getTimeInMillis();
            calendar.add(Calendar.MONTH, 1);
            long to = calendar.getTimeInMillis();
            Query bookingsQuery = FirebaseDatabase.getInstance().getReference()
                    .child("providerAppointments")
                    .child(providerKey)
                    .child("data")
                    .orderByChild("from")
                    .startAt(from)
                    .endAt(to);
            bookingsQuery.addChildEventListener(this);
            bookingsQueryList.put(key, bookingsQuery);
        }

        // Return only the events that matches newYear and newMonth.
        List<MyWeekViewEvent> matchedEvents = new ArrayList<>();
        for (MyWeekViewEvent event : events) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    @SuppressWarnings("WrongConstant")
    private boolean eventMatches(MyWeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    @NonNull
    private MyWeekViewEvent getWeekViewEvent(Booking booking) {
        GregorianCalendar from = new GregorianCalendar();
        from.setTime(new Date(booking.from));
        GregorianCalendar to = new GregorianCalendar();
        to.setTime(new Date(booking.to));
        String status = booking.getStatus() == BookingStatus.PENDING ? "⚠️" :
                booking.getStatus() < 0 ? "\uD83D\uDEAB" : "";
        MyWeekViewEvent event = new MyWeekViewEvent(
                booking.key.hashCode(),
                status,
                booking.serviceName,
                from,
                to,
                booking.key
        );
        float saturation = booking.getStatus() == BookingStatus.PENDING ? 1f :
                booking.getStatus() == BookingStatus.PROVIDER_ACCEPTED ? 0.4f : 0f;
        float value = 0.85f;
        float alpha = booking.getStatus() == BookingStatus.PENDING ? 1f : 0.75f;
        int color = ColorGenerator.getHsvColor(booking.serviceId, saturation, value, alpha);
        event.setColor(color);
        return event;
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        BookingDetailsDialog_.builder()
                .bookingKey(((MyWeekViewEvent) event).getBookingKey())
                .build()
                .show(getChildFragmentManager(), "BookingDetailsDialog");
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Booking booking = dataSnapshot.getValue(Booking.class);
        booking.key = dataSnapshot.getKey();
        if (showCancelled || (booking.getStatus() != BookingStatus.PROVIDER_REJECTED
                && booking.getStatus() != BookingStatus.PROVIDER_CANCELED
                && booking.getStatus() != BookingStatus.USER_CANCELED)) {
            MyWeekViewEvent event = getWeekViewEvent(booking);
            if (!events.contains(event)) events.add(event);
            weekView.notifyDatasetChanged();
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Booking booking = dataSnapshot.getValue(Booking.class);
        booking.key = dataSnapshot.getKey();
        MyWeekViewEvent event = getWeekViewEvent(booking);
        if (events.remove(event)) {
            if (booking.getStatus() != BookingStatus.PROVIDER_REJECTED
                    && booking.getStatus() != BookingStatus.PROVIDER_CANCELED
                    && booking.getStatus() != BookingStatus.USER_CANCELED) {
                events.add(event);
            }
            weekView.notifyDatasetChanged();
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Booking booking = dataSnapshot.getValue(Booking.class);
        booking.key = dataSnapshot.getKey();
        MyWeekViewEvent event = getWeekViewEvent(booking);
        if (events.remove(event)) {
            weekView.notifyDatasetChanged();
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Click(R.id.fab_add_booking)
    void addBooking() {
        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_planer_loading_services), PROGRESS_DELAY);
        FirebaseDatabase.getInstance().getReference()
                .child("providerServices")
                .child(providerKey)
                .child("data")
                .orderByChild("name")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            private int selected = 0;

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                final int cnt = (int) dataSnapshot.getChildrenCount();
                                final String[] names = new String[cnt];
                                final Service[] services = new Service[cnt];

                                int i = 0;
                                for (DataSnapshot child : children) {
                                    Service service = child.getValue(Service.class);
                                    if (service != null) {
                                        service.key = child.getKey();
                                        names[i] = service.name;
                                        services[i] = service;
                                        i++;
                                    }
                                }

                                progressDialog.dismiss();
                                progressDialog.cancel();

                                TextView titleView = new TextView(getContext());
                                titleView.setBackgroundColor(getResources().getColor(R.color.accent));
                                titleView.setTextColor(Color.WHITE);
                                titleView.setTextSize(20);

                                int horizontal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                                int vertical = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());

                                titleView.setPadding(horizontal, vertical, horizontal, vertical);
                                titleView.setText(R.string.planer_add_booking_title);

                                new android.app.AlertDialog.Builder(getContext())
//                                        .setTitle("Add appointment")
                                        .setCustomTitle(titleView)
                                        .setSingleChoiceItems(names, selected,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        selected = which;
                                                    }
                                                })
                                        .setPositiveButton(hr.nas2skupa.eleventhhour.common.R.string.action_pick, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                selectedService = services[selected];
                                                showDatePicker();
                                            }
                                        })
                                        .setNegativeButton(hr.nas2skupa.eleventhhour.common.R.string.action_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        }
                );
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                PlanerFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setTitle(selectedService.name);
        dpd.setMinDate(now);
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        pickedDateTime = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                PlanerFragment.this,
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
                                .userKey(null)
                                .providerKey(providerKey)
                                .serviceKey(selectedService.key)
                                .from(pickedDateTime)
                                .to(to)
                                .userName(null)
                                .providerName(provider.name)
                                .serviceName(selectedService.name)
                                .price(selectedService.price)
                                .build();
                        makeBookingDialog.setBookingDialogListener(PlanerFragment.this);
                        makeBookingDialog.show(getFragmentManager(), "MakeBookingDialog");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (getView() != null)
                            Snackbar.make(getView(), R.string.msg_booking_failed, Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    public void addCustomBooking(Booking booking) {
        booking.status = BookingStatus.PROVIDER_ACCEPTED;

        String key = FirebaseDatabase.getInstance().getReference()
                .child("providerAppointments")
                .child(providerKey)
                .child("data")
                .push().getKey();
        Map<String, Object> bookingValues = booking.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/providerAppointments/%s/data/%s", providerKey, key), bookingValues);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null)
                    Snackbar.make(getView(), R.string.msg_booking_failed, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBookingConfirmed(Booking booking) {
        addCustomBooking(booking);
    }

    @Override
    public void onBookingDismissed() {

    }


    private class MyWeekViewEvent extends WeekViewEvent {
        private String bookingKey;

        MyWeekViewEvent(long id, String name, String location, Calendar startTime, Calendar endTime, String bookingKey) {
            super(id, name, location, startTime, endTime);
            this.bookingKey = bookingKey;
        }

        String getBookingKey() {
            return bookingKey;
        }
    }
}
