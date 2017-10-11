package hr.nas2skupa.eleventhhour.common.utils;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by nas2skupa on 18/09/16.
 */

public class Utils {
    public static String getMyUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static String getLanguageIso() {
        return Locale.getDefault().getLanguage();
    }

    public static String getLocaleString(HashMap<String, String> strings) {
        if (strings.containsKey(getLanguageIso())) return strings.get(getLanguageIso());
        else return strings.get("def");
    }

    public static boolean isSameDay(Calendar d1, Calendar d2) {
        return d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR)
                && d1.get(Calendar.MONTH) == d2.get(Calendar.MONTH)
                && d1.get(Calendar.DAY_OF_MONTH) == d2.get(Calendar.DAY_OF_MONTH);
    }
}
