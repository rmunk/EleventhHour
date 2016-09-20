package hr.nas2skupa.eleventhhour.utils;

import java.util.Locale;

/**
 * Created by nas2skupa on 18/09/16.
 */

public class Utils {
    public static String getLanguageIso() {
        return Locale.getDefault().getISO3Language();
    }
}
