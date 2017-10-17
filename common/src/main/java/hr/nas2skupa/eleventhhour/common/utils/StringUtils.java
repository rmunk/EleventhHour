package hr.nas2skupa.eleventhhour.common.utils;

import java.util.Locale;

import hr.nas2skupa.eleventhhour.common.App;
import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;

/**
 * Created by nas2skupa on 30/10/2016.
 */

public class StringUtils {
    public static String printBookingStatus(@BookingStatus int status) {
        switch (status) {
            case BookingStatus.PENDING:
                return App.getAppContext().getString(R.string.booking_status_pending) + " \uD83D\uDCA4";
            case BookingStatus.PROVIDER_ACCEPTED:
                return App.getAppContext().getString(R.string.booking_status_confirmed) + " \uD83D\uDC4D";
            case BookingStatus.PROVIDER_REJECTED:
                return App.getAppContext().getString(R.string.booking_status_rejected) + " \uD83D\uDC4E";
            case BookingStatus.USER_CANCELED:
                return App.getAppContext().getString(R.string.booking_status_user_canceled) + " ✘";
            case BookingStatus.PROVIDER_CANCELED:
                return App.getAppContext().getString(R.string.booking_status_provider_canceled) + " ✘";
            case BookingStatus.FINISHED:
                return App.getAppContext().getString(R.string.booking_status_finished) + " ✔︎";
            default:
                return App.getAppContext().getString(R.string.booking_status_unknown);
        }
    }

    public static String minutesToTime(int minutes) {
        return String.format(Locale.getDefault(), "%d:%02d", minutes / 60, minutes % 60);
    }
}
