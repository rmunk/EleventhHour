package hr.nas2skupa.eleventhhour.common.model;

import com.google.firebase.database.Exclude;

import java.util.Calendar;

import hr.nas2skupa.eleventhhour.common.App;
import hr.nas2skupa.eleventhhour.common.R;

/**
 * Created by nas2skupa on 04/10/2017.
 */

public class OpenHours {
    public DailyHours mon;
    public DailyHours tue;
    public DailyHours wed;
    public DailyHours thu;
    public DailyHours fri;
    public DailyHours sat;
    public DailyHours sun;

    public OpenHours() {
        // Required empty public constructor
    }

    @Exclude
    public DailyHours getDailyHours(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return mon;
            case Calendar.TUESDAY:
                return tue;
            case Calendar.WEDNESDAY:
                return wed;
            case Calendar.THURSDAY:
                return thu;
            case Calendar.FRIDAY:
                return fri;
            case Calendar.SATURDAY:
                return sat;
            case Calendar.SUNDAY:
                return sun;
            default:
                return null;
        }
    }

    @Exclude
    public String today() {
        DailyHours today = getDailyHours(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        if (today == null) return App.getAppContext().getString(R.string.hours_unknown);
        else {
            if (today.open) {
                return App.getAppContext().getString(R.string.hours_open_today, today.toString());
            } else return App.getAppContext().getString(R.string.hours_closed_today);
        }
    }
}
