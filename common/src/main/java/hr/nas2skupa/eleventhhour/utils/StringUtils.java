package hr.nas2skupa.eleventhhour.utils;

import android.content.Context;
import android.location.Location;

import hr.nas2skupa.eleventhhour.common.R;
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
                return context.getString(R.string.booking_status_canceled) + " ✘";
            case BookingStatus.PROVIDER_CANCELED:
                return context.getString(R.string.booking_status_canceled) + " ✘";
            case BookingStatus.FINISHED:
                return context.getString(R.string.booking_status_finished) + " ✔︎";
            default:
                return context.getString(R.string.booking_status_unknown);
        }
    }
    public static String locationToDMS(Location location) {
        String output, degrees, minutes, seconds, direction;

        double decimal = location.getLatitude();

        direction = "N";
        if (decimal < 0) {
            decimal *= -1;
            direction = "S";
        }

        double mod = decimal % 1;
        degrees = String.valueOf((int) decimal);

        decimal = mod * 60;
        mod = decimal % 1;
        minutes = String.valueOf((int) decimal);

        decimal = mod * 60;
        seconds = String.valueOf((int) decimal);

        output = degrees + "°" + minutes + "'" + seconds + "\"" + direction;

        decimal = location.getLongitude();

        direction = "E";
        if (decimal < 0) {
            decimal *= -1;
            direction = "W";
        }

        mod = decimal % 1;
        degrees = String.valueOf((int) decimal);

        decimal = mod * 60;
        mod = decimal % 1;
        minutes = String.valueOf((int) decimal);

        decimal = mod * 60;
        seconds = String.valueOf((int) decimal);

        output += " " + degrees + "°" + minutes + "'" + seconds + "\"" + direction;

        return output;
    }
}
