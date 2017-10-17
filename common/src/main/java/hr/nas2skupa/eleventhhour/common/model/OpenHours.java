package hr.nas2skupa.eleventhhour.common.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.firebase.database.Exclude;

import java.util.Calendar;

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
    public DailyHours getHours(int dayOfWeek) {
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
    public DailyHours getHoursToday() {
        return getHours(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
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
