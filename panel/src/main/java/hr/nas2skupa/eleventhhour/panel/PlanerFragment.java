package hr.nas2skupa.eleventhhour.panel;


import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.MenuItem;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.common.utils.ColorGenerator;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_planer)
@OptionsMenu(R.menu.menu_planer)
public class PlanerFragment extends Fragment
        implements MonthLoader.MonthChangeListener,
        WeekView.EventClickListener,
        ChildEventListener {

    private static final int START_HOUR = 8;

    private List<MyWeekViewEvent> events = new ArrayList<>();
    private Map<String, Query> bookingsQueryList = new HashMap<>();
    private boolean showCancelled = false;

    @FragmentArg String providerKey;

    @ViewById WeekView weekView;

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
                    .child("bookings")
                    .child(providerKey)
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
