package hr.nas2skupa.eleventhhour.common.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IntRange;

import com.google.firebase.database.Exclude;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;

import hr.nas2skupa.eleventhhour.common.App;
import hr.nas2skupa.eleventhhour.common.BR;
import hr.nas2skupa.eleventhhour.common.R;

/**
 * Created by nas2skupa on 04/10/2017.
 */

public class OpenHours extends BaseObservable {
    private DailyHours mon;
    private DailyHours tue;
    private DailyHours wed;
    private DailyHours thu;
    private DailyHours fri;
    private DailyHours sat;
    private DailyHours sun;

    public OpenHours() {
        // Required empty public constructor
    }

    @Bindable
    public DailyHours getMon() {
        return mon;
    }

    public void setMon(DailyHours mon) {
        this.mon = mon;
        notifyPropertyChanged(BR.mon);
    }

    @Bindable
    public DailyHours getTue() {
        return tue;
    }

    public void setTue(DailyHours tue) {
        this.tue = tue;
        notifyPropertyChanged(BR.tue);
    }

    @Bindable
    public DailyHours getWed() {
        return wed;
    }

    public void setWed(DailyHours wed) {
        this.wed = wed;
        notifyPropertyChanged(BR.wed);
    }

    @Bindable
    public DailyHours getThu() {
        return thu;
    }

    public void setThu(DailyHours thu) {
        this.thu = thu;
        notifyPropertyChanged(BR.thu);
    }

    @Bindable
    public DailyHours getFri() {
        return fri;
    }

    public void setFri(DailyHours fri) {
        this.fri = fri;
        notifyPropertyChanged(BR.fri);
    }

    @Bindable
    public DailyHours getSat() {
        return sat;
    }

    public void setSat(DailyHours sat) {
        this.sat = sat;
        notifyPropertyChanged(BR.sat);
    }

    @Bindable
    public DailyHours getSun() {
        return sun;
    }

    public void setSun(DailyHours sun) {
        this.sun = sun;
        notifyPropertyChanged(BR.sun);
    }

    @Exclude
    public DailyHours getHours(@IntRange(from = 1L, to = 7L) int dayOfWeek) {
        switch (dayOfWeek) {
            case DateTimeConstants.MONDAY:
                return mon;
            case DateTimeConstants.TUESDAY:
                return tue;
            case DateTimeConstants.WEDNESDAY:
                return wed;
            case DateTimeConstants.THURSDAY:
                return thu;
            case DateTimeConstants.FRIDAY:
                return fri;
            case DateTimeConstants.SATURDAY:
                return sat;
            case DateTimeConstants.SUNDAY:
                return sun;
            default:
                return null;
        }
    }

    @Exclude
    public DailyHours getHoursToday() {
        return getHours(new LocalDateTime().getDayOfWeek());
    }

    @Exclude
    public String printHoursToday() {
        DailyHours today = getHoursToday();
        if (today == null) return App.getAppContext().getString(R.string.hours_unknown);
        else {
            if (today.isOpen()) {
                return App.getAppContext().getString(R.string.hours_open_today, today.toString());
            } else return App.getAppContext().getString(R.string.hours_closed_today);
        }
    }

    @Exclude
    public boolean areValid() {
        return mon.isValid() &&
                tue.isValid() &&
                wed.isValid() &&
                thu.isValid() &&
                fri.isValid() &&
                sat.isValid() &&
                sun.isValid();
    }
}
