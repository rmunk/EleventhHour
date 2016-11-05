package hr.nas2skupa.eleventhhour.utils;

import android.content.Context;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.BookingStatus;

/**
 * Created by nas2skupa on 30/10/2016.
 */

public class StringUtils {
    public static String printBookingStatus(Context context, @BookingStatus int status) {
        switch (status) {
            case BookingStatus.PENDING:
                return context.getString(R.string.booking_status_pending) + " \uD83D\uDCA4";
            case BookingStatus.PROVIDER_ACCEPTED:
                return context.getString(R.string.booking_status_confirmed) + " \uD83D\uDC4D";
            case BookingStatus.PROVIDER_REJECTED:
                return context.getString(R.string.booking_status_rejected) + " \uD83D\uDC4E";
            case BookingStatus.USER_CANCELED:
                return context.getString(R.string.booking_status_canceled) + " ✗";
            default:
                return context.getString(R.string.booking_status_unknown);
        }
    }
}
