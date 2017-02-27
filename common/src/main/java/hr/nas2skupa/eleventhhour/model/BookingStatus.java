package hr.nas2skupa.eleventhhour.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by nas2skupa on 22/10/2016.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        BookingStatus.PENDING,
        BookingStatus.PROVIDER_ACCEPTED,
        BookingStatus.PROVIDER_REJECTED,
        BookingStatus.USER_CANCELED,
        BookingStatus.FINISHED
})
public @interface BookingStatus {
    /***
     * Booking has been sent, waiting for provider confirmation.
     */
    int PENDING = 0;

    /***
     * Booking has been accepted by the provider.
     */
    int PROVIDER_ACCEPTED = 1;

    /***
     * Provider has rejected booking.
     */
    int PROVIDER_REJECTED = -1;

    /***
     * User has canceled booking after provider confirmation.
     */
    int USER_CANCELED = -2;
    /***
     * Booking has already finished.
     */
    int FINISHED = -3;
}
