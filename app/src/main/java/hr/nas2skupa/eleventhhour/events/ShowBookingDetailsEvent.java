package hr.nas2skupa.eleventhhour.events;

import hr.nas2skupa.eleventhhour.model.Booking;

/**
 * Created by nas2skupa on 23/10/2016.
 */

public class ShowBookingDetailsEvent {
    Booking booking;

    public ShowBookingDetailsEvent(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
