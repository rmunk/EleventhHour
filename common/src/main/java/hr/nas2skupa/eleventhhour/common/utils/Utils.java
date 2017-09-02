package hr.nas2skupa.eleventhhour.common.utils;

import com.google.firebase.auth.FirebaseAuth;

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
        return Locale.getDefault().getISO3Language();
    }

    public static String getLocaleString(HashMap<String, String> strings) {
        if (strings.containsKey(getLanguageIso())) return strings.get(getLanguageIso());
        else return strings.get("def");
    }
}
