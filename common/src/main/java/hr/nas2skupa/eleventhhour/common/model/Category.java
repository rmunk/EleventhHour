package hr.nas2skupa.eleventhhour.common.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.common.utils.Utils;

/**
 * Created by nas2skupa on 08/09/16.
 */

public class Category {
    public HashMap<String, String> name;
    public String color;
    public String icon;

    public Category() {
    }

    @Exclude
    public String getLocalName() {
        return Utils.getLocaleString(name);
    }
}
