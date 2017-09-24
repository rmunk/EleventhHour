package hr.nas2skupa.eleventhhour.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import hr.nas2skupa.eleventhhour.events.CancelBookingEvent;
import hr.nas2skupa.eleventhhour.events.ShowBookingDetailsEvent;
import hr.nas2skupa.eleventhhour.ui.viewholders.BookingViewHolder;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_calendar)
public class CalendarFragment extends Fragment {

    @ColorRes(R.color.event_active)
    int eventActive;
    @ColorRes(R.color.event_inactive)
    int eventInactive;

    @ViewById(R.id.main_layout)
    ViewGroup mainLayout;
    @ViewById(R.id.calendar)
    CompactCalendarView calendarView;
    @ViewById(R.id.txt_month)
    TextView txtMonth;
    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private DatabaseReference bookingsReference;
    private Query bookingsQuery;
    private ChildEventListener bookingsChangedListener;
    private FirebaseRecyclerAdapter<Booking, BookingViewHolder> adapter;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingsReference = FirebaseDatabase.getInstance().getReference()
                .child("userAppointments")
                .child(Utils.getMyUid())
                .child("data");

        bookingsChangedListener = new BookingsChangedListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bookingsChangedListener != null) {
            bookingsQuery.removeEventListener(bookingsChangedListener);
            bookingsChangedListener = null;
        }
        if (adapter != null) adapter.cleanup();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void showBooking(ShowBookingDetailsEvent event) {
        BookingDetailsDialog_.builder()
                .bookingKey(event.getBooking().key)
                .build()
                .show(getFragmentManager(), "BookingDetailsDialog");
    }

    @Subscribe
    public void cancelBooking(CancelBookingEvent event) {
        Booking booking = event.getBooking();
        if (booking == null) {
            Snackbar.make(getView(), R.string.msg_booking_cancel_failed, Snackbar.LENGTH_LONG).show();
            return;
        }

        // Shouldn't cancel
        if (booking.getStatus() < 0 || booking.to <= new Date().getTime())
            return;

        booking.status = BookingStatus.USER_CANCELED;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = booking.key;
        Map<String, Object> bookingValues = booking.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/providerAppointments/%s/data/%s", booking.providerId, key), bookingValues);
        childUpdates.put(String.format("/userAppointments/%s/data/%s", Utils.getMyUid(), key), bookingValues);
        reference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null)
                    Snackbar.make(getView(), R.string.msg_booking_cancel_failed, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @AfterViews
    public void afterViews() {
        Date now = new Date();
        setCalendarTitle(now);

        calendarView.setCurrentDate(now);
        calendarView.setUseThreeLetterAbbreviation(true);
        calendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        calendarView.setListener(new CalendarListener());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemViewCacheSize(0);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);

        Calendar nextMonth = new GregorianCalendar();
        nextMonth.setTime(calendarView.getFirstDayOfCurrentMonth());
        nextMonth.add(Calendar.MONTH, 1);
        Date firstDayOfNextMonth = nextMonth.getTime();

        addDateRangeListener(calendarView.getFirstDayOfCurrentMonth(), firstDayOfNextMonth, new Date());
        setBookingsAdapter(now);
    }

    private void setCalendarTitle(Date date) {
        txtMonth.setText(DateUtils.formatDateTime(
                getContext(),
                date.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
        ));
    }

    private void addDateRangeListener(Date from, Date to, Date selected) {
        calendarView.removeAllEvents();
        calendarView.setCurrentDate(selected);

        bookingsQuery = bookingsReference
                .orderByChild("from")
                .startAt(from.getTime())
                .endAt(to.getTime());

        bookingsQuery.addChildEventListener(bookingsChangedListener);
    }

    private void setBookingsAdapter(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date start = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date end = calendar.getTime();

        Query query = bookingsReference
                .orderByChild("from")
                .startAt(start.getTime())
                .endAt(end.getTime());

        if (adapter != null) adapter.cleanup();
        adapter = new FirebaseRecyclerAdapter<Booking, BookingViewHolder>(
                Booking.class,
                R.layout.item_booking,
                BookingViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final BookingViewHolder viewHolder, final Booking model, final int position) {
                model.key = getRef(position).getKey();
                viewHolder.bind(getContext(), model);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private class CalendarListener implements CompactCalendarView.CompactCalendarViewListener {

        @Override
        public void onDayClick(Date dateClicked) {
            setBookingsAdapter(dateClicked);
            setCalendarTitle(dateClicked);
        }

        @Override
        public void onMonthScroll(Date firstDayOfNewMonth) {
            if (bookingsChangedListener != null)
                bookingsQuery.removeEventListener(bookingsChangedListener);

            Calendar nextMonth = new GregorianCalendar();
            nextMonth.setTime(firstDayOfNewMonth);
            nextMonth.add(Calendar.MONTH, 1);
            Date firstDayOfNextMonth = nextMonth.getTime();

            Date now = new Date();
            final Date selectedDate = firstDayOfNewMonth.before(now) && firstDayOfNextMonth.after(now) ? now : firstDayOfNewMonth;

            setCalendarTitle(firstDayOfNewMonth);
            addDateRangeListener(firstDayOfNewMonth, firstDayOfNextMonth, selectedDate);
            setBookingsAdapter(selectedDate);
        }

    }

    private class BookingsChangedListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Booking booking = dataSnapshot.getValue(Booking.class);
            booking.key = dataSnapshot.getKey();
            int color = booking.getStatus() >= 0 && booking.to > new Date().getTime() ? eventActive : eventInactive;
            calendarView.addEvent(new Event(color, booking.from, booking), true);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Booking booking = dataSnapshot.getValue(Booking.class);
            booking.key = dataSnapshot.getKey();
            removeBookingFromCalendar(booking);
            int color = booking.getStatus() > 0 && booking.to > new Date().getTime() ? eventActive : eventInactive;
            calendarView.addEvent(new Event(color, booking.from, booking), true);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Booking booking = dataSnapshot.getValue(Booking.class);
            booking.key = dataSnapshot.getKey();
            removeBookingFromCalendar(booking);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        private void removeBookingFromCalendar(@NonNull Booking booking) {
            List<Event> events = calendarView.getEventsForMonth(calendarView.getFirstDayOfCurrentMonth());
            for (Event event : events) {
                Booking oldBooking = (Booking) event.getData();
                if (booking.equals(oldBooking)) calendarView.removeEvent(event, true);
            }
        }
    }
}
