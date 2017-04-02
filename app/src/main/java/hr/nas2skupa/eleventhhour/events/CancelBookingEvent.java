package hr.nas2skupa.eleventhhour.events;

import hr.nas2skupa.eleventhhour.common.model.Booking;

/**
 * Created by nas2skupa on 04/11/2016.
 */

public class CancelBookingEvent {
    private Booking booking;

    public CancelBookingEvent(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
