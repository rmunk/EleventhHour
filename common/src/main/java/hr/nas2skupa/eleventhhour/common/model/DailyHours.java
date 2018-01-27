package hr.nas2skupa.eleventhhour.common.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.firebase.database.Exclude;

import net.danlew.android.joda.DateUtils;

import org.joda.time.LocalTime;

import hr.nas2skupa.eleventhhour.common.App;
import hr.nas2skupa.eleventhhour.common.BR;
import hr.nas2skupa.eleventhhour.common.R;

/**
 * Created by nas2skupa on 04/10/2017.
 */
public class DailyHours extends BaseObservable {
    private String from;
    private String to;
    private boolean open;

    public DailyHours() {
        // Required empty public constructor
    }

    @Bindable
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
        notifyPropertyChanged(BR.from);
    }

    @Bindable
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
        notifyPropertyChanged(BR.to);
    }

    @Bindable
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        notifyPropertyChanged(BR.open);
    }

    @Exclude
    public LocalTime getFromDate() {
        if (!open) return null;
        try {
            return LocalTime.parse(from);
        } catch (Exception e) {
            return null;
        }
    }

    @Exclude
    public LocalTime getToDate() {
        if (!open) return null;
        try {
            return LocalTime.parse(to);
        } catch (Exception e) {
            return null;
        }
    }

    @Exclude
    @Override
    public String toString() {
        if (open) {
            if (getFromDate() == null || getToDate() == null) {
                return App.getAppContext().getString(R.string.hours_unknown);
            }
            return DateUtils.formatDateRange(App.getAppContext(), getFromDate(), getToDate(), DateUtils.FORMAT_SHOW_TIME);
        } else return App.getAppContext().getString(R.string.hours_closed);
    }

    @Exclude
    public boolean isValid() {
        return !open || (getFromDate() != null && getToDate() != null);
    }
}
