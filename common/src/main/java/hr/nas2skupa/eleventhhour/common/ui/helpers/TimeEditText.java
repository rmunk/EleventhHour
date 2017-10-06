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
        Pattern pattern = Pattern.compile("^\\d+:*\\d*");
        Matcher matcher = pattern.matcher(getText());
        cursor = matcher.find() ? matcher.end() : 0;
        setSelection(cursor);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (time == null) time = HH_MM.toCharArray();
        if (Objects.equals(s.toString(), String.valueOf(time))) return;

        if (start >= time.length) {
            setText(String.valueOf(time));
            return;
        }

        for (int i = start; i > start - before && i >= 0; i--) {
            if (time[i] == ':') i--;
            if (i >= 0) time[i] = HH_MM.charAt(i);
        }

        if (s.length() == time.length + 1) time[start] = s.charAt(start);

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
    }
}
