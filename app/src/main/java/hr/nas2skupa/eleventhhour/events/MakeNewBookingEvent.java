package hr.nas2skupa.eleventhhour.events;

import hr.nas2skupa.eleventhhour.common.model.Booking;

/**
 * Created by nas2skupa on 23/10/2016.
 */

public class MakeNewBookingEvent {
    Booking booking;

    public MakeNewBookingEvent(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
