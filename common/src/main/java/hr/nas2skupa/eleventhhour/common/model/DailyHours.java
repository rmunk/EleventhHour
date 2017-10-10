package hr.nas2skupa.eleventhhour.common.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.format.DateFormat;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            if (getFromDate() == null || getToDate() == null) {
                return App.getAppContext().getString(R.string.hours_unknown);
            }
            java.text.DateFormat timeFormat = DateFormat.getTimeFormat(App.getAppContext());
            return timeFormat.format(getFromDate()) + "–" + timeFormat.format(getToDate());
        } else return App.getAppContext().getString(R.string.hours_closed);
    }

    @Exclude
    public boolean isValid() {
        return !open || (getFromDate() != null && getToDate() != null);
    }
}
