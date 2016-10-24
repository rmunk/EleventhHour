package hr.nas2skupa.eleventhhour.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Booking;
import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_calendar)
public class CalendarFragment extends Fragment {

    @ColorRes(R.color.colorAccent)
    int colorAccent;

    @ViewById(R.id.main_layout)
    ViewGroup mainLayout;
    @ViewById(R.id.calendar)
    CompactCalendarView calendarView;
    @ViewById(R.id.txt_month)
    TextView txtMonth;

    private DatabaseReference bookingsReference;
    private Query bookingsQuery;
    private ChildEventListener bookingsChangedListener;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingsReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("bookings");
    }

    @Override
    public void onStart() {
        super.onStart();

        calendarView.removeAllEvents();
        bookingsChangedListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Booking booking = dataSnapshot.getValue(Booking.class);
                booking.setKey(dataSnapshot.getKey());
                calendarView.addEvent(new Event(colorAccent, booking.getFrom(), booking), true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Booking booking = dataSnapshot.getValue(Booking.class);
                booking.setKey(dataSnapshot.getKey());
                removeBookingFromCalendar(booking);
                calendarView.addEvent(new Event(colorAccent, booking.getFrom(), booking), true);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Booking booking = dataSnapshot.getValue(Booking.class);
                booking.setKey(dataSnapshot.getKey());
                removeBookingFromCalendar(booking);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        Date currentMonth = calendarView.getFirstDayOfCurrentMonth();
        Calendar nextMonth = new GregorianCalendar();
        nextMonth.setTime(currentMonth);
        nextMonth.add(Calendar.MONTH, 1);
        bookingsQuery = bookingsReference
                .orderByChild("from")
                .startAt(currentMonth.getTime())
                .endAt(nextMonth.getTime().getTime());
        bookingsQuery.addChildEventListener(bookingsChangedListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (bookingsChangedListener != null)
            bookingsQuery.removeEventListener(bookingsChangedListener);
    }

    @AfterViews
    public void afterViews() {
        setCalendarTitle(calendarView.getFirstDayOfCurrentMonth());

        calendarView.setUseThreeLetterAbbreviation(true);
        calendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                List<Event> events = calendarView.getEvents(dateClicked);

                setCalendarTitle(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar nextMonth = new GregorianCalendar();
                nextMonth.setTime(firstDayOfNewMonth);
                nextMonth.add(Calendar.MONTH, 1);

                calendarView.removeAllEvents();
                if (bookingsChangedListener != null)
                    bookingsQuery.removeEventListener(bookingsChangedListener);
                bookingsQuery = bookingsReference
                        .orderByChild("from")
                        .startAt(firstDayOfNewMonth.getTime())
                        .endAt(nextMonth.getTime().getTime());
                bookingsQuery.addChildEventListener(bookingsChangedListener);

                setCalendarTitle(firstDayOfNewMonth);
            }
        });
    }

    private void removeBookingFromCalendar(Booking booking) {
        List<Event> events = calendarView.getEventsForMonth(calendarView.getFirstDayOfCurrentMonth());
        for (Event event : events) {
            Booking oldBooking = (Booking) event.getData();
            if (oldBooking.equals(booking))
                calendarView.removeEvent(event, true);
        }
    }

    private void setCalendarTitle(Date date) {
        txtMonth.setText(DateUtils.formatDateTime(
                getContext(),
                date.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
        ));
    }
}
