package hr.nas2skupa.eleventhhour.common;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by nas2skupa on 24/09/2017.
 */

@SharedPref
public interface Preferences {
    @DefaultString("HR")
    String country();

    String city();
}