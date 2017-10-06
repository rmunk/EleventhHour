package hr.nas2skupa.eleventhhour.common.model;

import android.text.format.DateFormat;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.common.App;
import hr.nas2skupa.eleventhhour.common.R;

/**
 * Created by nas2skupa on 04/10/2017.
 */
public class DailyHours {
    public String from;
    public String to;
    public boolean open;

    public DailyHours() {
        // Required empty public constructor
    }

    @Exclude
    public Date getFromDate() {
        if (!open) return null;
        try {
            return new SimpleDateFormat("HH:mm", Locale.US).parse(from);
        } catch (ParseException e) {
            return null;
        }
    }

    @Exclude
    public Date getToDate() {
        if (!open) return null;
        try {
            return new SimpleDateFormat("HH:mm", Locale.US).parse(to);
        } catch (ParseException e) {
            return null;
        }
    }

    @Exclude
    @Override
    public String toString() {
        if (open) {
            if (from == null || to == null) {
                return App.getAppContext().getString(R.string.hours_unknown);
            }
            java.text.DateFormat timeFormat = DateFormat.getTimeFormat(App.getAppContext());
            return timeFormat.format(getFromDate()) + "–" + timeFormat.format(getToDate());
        } else return App.getAppContext().getString(R.string.hours_closed);
    }
}
