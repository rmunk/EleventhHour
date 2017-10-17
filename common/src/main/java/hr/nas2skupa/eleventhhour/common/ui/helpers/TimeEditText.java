package hr.nas2skupa.eleventhhour.common.ui.helpers;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nas2skupa on 05/10/2017.
 */

public class TimeEditText extends android.support.v7.widget.AppCompatEditText {
    private static final String HH_MM = "  :  ";
    private char[] time = HH_MM.toCharArray();
    private int cursor = 0;

    public TimeEditText(Context context) {
        super(context);
    }

    public TimeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (cursor == selStart) return;
        adjustSelection();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // for some reason time cannot be set on declaration or in constructor
        if (time == null) {
            time = HH_MM.toCharArray();
            setText(String.valueOf(time));
            return;
        }
        if (Objects.equals(s.toString(), String.valueOf(time))) return;

        if (count == 1 && start < HH_MM.length()) {                             // User typed a character
            time[start] = s.charAt(start);
        } else if (before == 1) {                                               // User deleted a character
            if (time[start] == ':') start--;
            time[start] = HH_MM.charAt(start);
        } else if (count == 5
                && s.toString().matches("^([0-1]\\d|2[0-3]):[0-5]\\d$")) {      // Time was set form code
            time = s.toString().toCharArray();
        } else {                                                                // Text is bullshit so drop it
            setText(String.valueOf(time));
            return;
        }

        try {
            int hour = Integer.parseInt(String.valueOf(time, 0, 2));
            hour = hour <= 23 ? hour : 23;
            String hourStr = String.format(Locale.US, "%02d", hour);
            time[0] = hourStr.charAt(0);
            time[1] = hourStr.charAt(1);
        } catch (NumberFormatException ignored) {
        }
        try {
            int minute = Integer.parseInt(String.valueOf(time, 3, 2));
            minute = minute <= 59 ? minute : 59;
            String minuteStr = String.format(Locale.US, "%02d", minute);
            time[3] = minuteStr.charAt(0);
            time[4] = minuteStr.charAt(1);
        } catch (NumberFormatException ignored) {
        }

        setText(String.valueOf(time));
        adjustSelection();
    }

    private void adjustSelection() {
        Pattern pattern = Pattern.compile("^\\d{2}:\\d{0,2}|^\\d");
        Matcher matcher = pattern.matcher(getText());
        cursor = matcher.find() ? matcher.end() : 0;
        setSelection(cursor);
    }
}
